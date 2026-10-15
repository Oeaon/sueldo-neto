package com.sueldoneto.core

import kotlin.math.max
import kotlin.math.min

data class UnemploymentResult(
    /** Días de prestación; 0 si no llega al mínimo de 360 días cotizados. */
    val days: Int,
    /** Importe bruto mensual en cada tramo: días 1-180, 181-360 y desde el 361. */
    val first180: Double,
    val from181: Double,
    val from361: Double,
) {
    val hasRight: Boolean get() = days > 0
}

/** Prestación contributiva por desempleo (paro) tras la reforma de noviembre de 2024. */
object UnemploymentCalculator {
    fun calculate(
        averageMonthlyBase: Double,
        daysContributedLast6Years: Int,
        children: Int,
        tax: TaxYear = TaxYears.current,
    ): UnemploymentResult {
        val days = durationDays(daysContributedLast6Years)
        if (days == 0) return UnemploymentResult(0, 0.0, 0.0, 0.0)
        val dailyBase = averageMonthlyBase / 30
        // Límites sobre el IPREM incrementado en una sexta parte (pagas extras).
        val iprem = tax.monthlyIprem * 7 / 6
        val maxFactor = when {
            children <= 0 -> 1.75
            children == 1 -> 2.00
            else -> 2.25
        }
        val minFactor = if (children <= 0) 0.80 else 1.07
        fun monthly(percent: Double) = min(max(dailyBase * 30 * percent, iprem * minFactor), iprem * maxFactor)
        return UnemploymentResult(days, monthly(0.70), monthly(0.60), monthly(0.50))
    }

    /** Tabla del art. 269 de la Ley General de la Seguridad Social: 120 días por 360 cotizados, +60 cada 180, máximo 720. */
    fun durationDays(daysContributed: Int): Int {
        if (daysContributed < 360) return 0
        val steps = (daysContributed - 360) / 180
        return min(120 + steps * 60, 720)
    }
}
