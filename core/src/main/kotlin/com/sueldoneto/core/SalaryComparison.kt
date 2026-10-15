package com.sueldoneto.core

/** Diferencia entre dos ofertas (B menos A). */
data class SalaryComparison(val a: SalaryResult, val b: SalaryResult) {
    val netAnnualDifference: Double get() = b.netAnnual - a.netAnnual
    val netMonthlyDifference: Double get() = netAnnualDifference / 12

    /** Cuánto de cada euro bruto de más llega al bolsillo (tipo marginal efectivo inverso). */
    val keptPerExtraEuro: Double?
        get() {
            val grossDiff = b.grossAnnual - a.grossAnnual
            return if (grossDiff == 0.0) null else netAnnualDifference / grossDiff
        }
}
