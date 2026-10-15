package com.sueldoneto.core

import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

enum class ContractType { PERMANENT, TEMPORARY }

data class SalaryInput(
    val grossAnnual: Double,
    /** 12 o 14 pagas. */
    val payments: Int = 14,
    val contract: ContractType = ContractType.PERMANENT,
    val children: Int = 0,
    val childrenUnder3: Int = 0,
)

data class SalaryResult(
    val grossAnnual: Double,
    val socialSecurity: Double,
    /** Tipo de retención en porcentaje con dos decimales, como en la nómina. */
    val withholdingRate: Double,
    val withholding: Double,
    val payments: Int,
) {
    val netAnnual: Double get() = grossAnnual - socialSecurity - withholding

    /** Neto de un mes normal. Con 14 pagas la Seguridad Social se reparte en 12 meses (extras prorrateadas). */
    val netMonthly: Double get() = grossAnnual / payments - socialSecurity / 12 - withholding / payments

    /** Neto de cada paga extra (solo con 14 pagas): no cotiza aparte, solo retiene IRPF. */
    val netExtraPay: Double? get() = if (payments == 14) grossAnnual / 14 * (1 - withholdingRate / 100) else null
}

/**
 * Nómina española por el procedimiento general de retenciones del IRPF (art. 80-86 del Reglamento).
 * Supuestos: contribuyente sin cónyuge a cargo, menor de 65 años, sin discapacidad, hijos
 * compartidos con el otro progenitor (mínimo por descendientes al 50 %). No vale para País Vasco ni Navarra.
 */
object SalaryCalculator {
    fun calculate(input: SalaryInput, tax: TaxYear = TaxYears.current): SalaryResult {
        require(input.payments == 12 || input.payments == 14)
        val gross = max(input.grossAnnual, 0.0)
        val socialSecurity = socialSecurity(gross, input.contract, tax)
        val rate = withholdingRate(gross, socialSecurity, input, tax)
        return SalaryResult(
            grossAnnual = gross,
            socialSecurity = socialSecurity,
            withholdingRate = rate,
            withholding = gross * rate / 100,
            payments = input.payments,
        )
    }

    fun socialSecurity(gross: Double, contract: ContractType, tax: TaxYear): Double {
        val rate = if (contract == ContractType.PERMANENT) tax.socialSecurityRatePermanent else tax.socialSecurityRateTemporary
        val monthly = gross / 12
        val base = min(monthly, tax.maxMonthlyBase)
        return (base * rate + solidarity(monthly, tax)) * 12
    }

    /** Cuota de solidaridad del trabajador sobre lo que pasa de la base máxima. */
    private fun solidarity(monthly: Double, tax: TaxYear): Double {
        val excess = monthly - tax.maxMonthlyBase
        if (excess <= 0) return 0.0
        val limits = listOf(tax.maxMonthlyBase * 0.10, tax.maxMonthlyBase * 0.50, Double.MAX_VALUE)
        var previous = 0.0
        var total = 0.0
        limits.forEachIndexed { i, limit ->
            val inBracket = (min(excess, limit) - previous).coerceAtLeast(0.0)
            total += inBracket * tax.solidarityRates[i]
            previous = limit
        }
        return total * tax.solidarityWorkerShare
    }

    fun withholdingRate(gross: Double, socialSecurity: Double, input: SalaryInput, tax: TaxYear): Double {
        val limitIndex = input.children.coerceIn(0, tax.noWithholdingLimits.size - 1)
        val noWithholdingLimit = tax.noWithholdingLimits[limitIndex]
        if (gross <= noWithholdingLimit) return 0.0

        val netWork = max(gross - socialSecurity - tax.otherDeductibleExpenses, 0.0)
        val base = max(netWork - workIncomeReduction(netWork), 0.0)
        val minimum = personalAndFamilyMinimum(input, tax)
        val quota = max(scale(base, tax) - scale(minimum, tax), 0.0)
        val capped = min(quota, (gross - noWithholdingLimit) * tax.withholdingCapRate)
        return round(capped / gross * 100 * 100) / 100
    }

    /** Reducción por obtención de rendimientos del trabajo (art. 20 de la Ley del IRPF). */
    fun workIncomeReduction(netWork: Double): Double = when {
        netWork <= 14_852.0 -> 7_302.0
        netWork <= 17_673.52 -> 7_302.0 - 1.75 * (netWork - 14_852.0)
        netWork <= 19_747.50 -> 2_364.34 - 1.14 * (netWork - 17_673.52)
        else -> 0.0
    }

    private fun personalAndFamilyMinimum(input: SalaryInput, tax: TaxYear): Double {
        val children = (0 until input.children.coerceAtLeast(0)).sumOf { i ->
            tax.childMinimums[min(i, tax.childMinimums.size - 1)]
        }
        val under3 = input.childrenUnder3.coerceIn(0, input.children) * tax.childUnder3Extra
        return tax.personalMinimum + (children + under3) / 2
    }

    fun scale(amount: Double, tax: TaxYear): Double {
        var previous = 0.0
        var total = 0.0
        for (bracket in tax.withholdingScale) {
            if (amount <= previous) break
            total += (min(amount, bracket.upTo) - previous) * bracket.rate
            previous = bracket.upTo
        }
        return total
    }
}
