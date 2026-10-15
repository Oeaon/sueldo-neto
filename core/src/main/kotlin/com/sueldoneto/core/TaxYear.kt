package com.sueldoneto.core

/**
 * Valores fiscales y de cotización de un año. Todos los importes en euros.
 * Al cambiar de año basta con añadir otra instancia y revisar cada valor con el BOE.
 */
data class TaxYear(
    val year: Int,
    /** Cuota del trabajador: contingencias comunes, formación profesional y MEI (en tanto por uno). */
    val commonRate: Double,
    val trainingRate: Double,
    val meiRate: Double,
    val unemploymentRatePermanent: Double,
    val unemploymentRateTemporary: Double,
    /** Base máxima de cotización mensual. */
    val maxMonthlyBase: Double,
    /** Cuota de solidaridad (total empresa + trabajador) por tramos del exceso sobre la base máxima. */
    val solidarityRates: List<Double>,
    /** Parte de la cuota de solidaridad que paga el trabajador (la misma proporción que en contingencias comunes). */
    val solidarityWorkerShare: Double,
    /** Escala de retenciones (art. 85 del Reglamento del IRPF): límites superiores y tipos. */
    val withholdingScale: List<Bracket>,
    val otherDeductibleExpenses: Double,
    val personalMinimum: Double,
    val childMinimums: List<Double>,
    val childUnder3Extra: Double,
    /** Sueldos hasta este importe no tienen retención (contribuyente sin cónyuge a cargo), por número de hijos 0, 1, 2+. */
    val noWithholdingLimits: List<Double>,
    /** Tope de la retención: este porcentaje del exceso sobre el límite sin retención. */
    val withholdingCapRate: Double,
    /** IPREM mensual, para los límites del paro. */
    val monthlyIprem: Double,
) {
    val socialSecurityRatePermanent: Double get() = commonRate + trainingRate + meiRate + unemploymentRatePermanent
    val socialSecurityRateTemporary: Double get() = commonRate + trainingRate + meiRate + unemploymentRateTemporary
}

data class Bracket(val upTo: Double, val rate: Double)

object TaxYears {
    /**
     * 2026. Revisar con el BOE antes de publicar: base máxima, tipos de la cuota de solidaridad,
     * MEI, límites sin retención e IPREM (los de 2025 si no se han actualizado).
     */
    val Y2026 = TaxYear(
        year = 2026,
        commonRate = 0.0470,
        trainingRate = 0.0010,
        meiRate = 0.0015,
        unemploymentRatePermanent = 0.0155,
        unemploymentRateTemporary = 0.0160,
        maxMonthlyBase = 5101.20,
        solidarityRates = listOf(0.0115, 0.0125, 0.0146),
        solidarityWorkerShare = 4.70 / 28.30,
        withholdingScale = listOf(
            Bracket(12_450.0, 0.19),
            Bracket(20_200.0, 0.24),
            Bracket(35_200.0, 0.30),
            Bracket(60_000.0, 0.37),
            Bracket(300_000.0, 0.45),
            Bracket(Double.MAX_VALUE, 0.47),
        ),
        otherDeductibleExpenses = 2_000.0,
        personalMinimum = 5_550.0,
        childMinimums = listOf(2_400.0, 2_700.0, 4_000.0, 4_500.0),
        childUnder3Extra = 2_800.0,
        noWithholdingLimits = listOf(15_876.0, 16_342.0, 16_867.0),
        withholdingCapRate = 0.43,
        monthlyIprem = 600.0,
    )

    val current: TaxYear = Y2026
}
