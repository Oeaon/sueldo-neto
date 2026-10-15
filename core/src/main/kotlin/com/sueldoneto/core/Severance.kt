package com.sueldoneto.core

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.min

enum class DismissalType(val daysPerYear: Int, val maxMonths: Int?) {
    /** Despido improcedente (contratos desde el 12/02/2012). */
    UNFAIR(33, 24),
    /** Despido objetivo. */
    OBJECTIVE(20, 12),
    /** Fin de contrato temporal. */
    END_OF_TEMPORARY(12, null),
    /** Baja voluntaria o despido disciplinario procedente: sin indemnización. */
    NONE(0, null),
}

data class SeveranceResult(val compensation: Double, val vacationPay: Double, val yearsWorked: Double) {
    val total: Double get() = compensation + vacationPay
}

object SeveranceCalculator {
    /**
     * Indemnización por años trabajados (con su tope en mensualidades) más vacaciones no disfrutadas.
     * El salario diario es el bruto anual entre 365. No incluye la parte del mes ni las pagas extras devengadas.
     */
    fun calculate(
        grossAnnual: Double,
        start: LocalDate,
        end: LocalDate,
        type: DismissalType,
        pendingVacationDays: Int,
    ): SeveranceResult {
        require(!end.isBefore(start))
        val daily = grossAnnual / 365
        val years = ChronoUnit.DAYS.between(start, end) / 365.0
        val uncapped = daily * type.daysPerYear * years
        val cap = type.maxMonths?.let { grossAnnual / 12 * it } ?: Double.MAX_VALUE
        return SeveranceResult(
            compensation = min(uncapped, cap),
            vacationPay = daily * pendingVacationDays.coerceAtLeast(0),
            yearsWorked = years,
        )
    }
}
