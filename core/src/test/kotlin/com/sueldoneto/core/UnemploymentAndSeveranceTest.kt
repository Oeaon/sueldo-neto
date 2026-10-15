package com.sueldoneto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate

class UnemploymentAndSeveranceTest {
    @Test
    fun `duracion del paro segun dias cotizados`() {
        assertEquals(0, UnemploymentCalculator.durationDays(359))
        assertEquals(120, UnemploymentCalculator.durationDays(360))
        assertEquals(120, UnemploymentCalculator.durationDays(539))
        assertEquals(180, UnemploymentCalculator.durationDays(540))
        assertEquals(720, UnemploymentCalculator.durationDays(2_160))
        assertEquals(720, UnemploymentCalculator.durationDays(5_000))
    }

    @Test
    fun `importe del paro 70 60 50 con topes`() {
        val result = UnemploymentCalculator.calculate(1_500.0, 1_000, children = 0)

        assertEquals(1_050.0, result.first180, 1e-9)
        assertEquals(900.0, result.from181, 1e-9)
        assertEquals(750.0, result.from361, 1e-9)
    }

    @Test
    fun `el paro tiene tope maximo y minimo`() {
        val iprem = 600.0 * 7 / 6
        val high = UnemploymentCalculator.calculate(4_000.0, 2_000, children = 0)
        val low = UnemploymentCalculator.calculate(500.0, 2_000, children = 1)

        assertEquals(iprem * 1.75, high.first180, 1e-9)
        assertEquals(iprem * 1.07, low.from361, 1e-9)
    }

    @Test
    fun `sin 360 dias no hay derecho a paro`() {
        assertFalse(UnemploymentCalculator.calculate(1_500.0, 200, 0).hasRight)
    }

    @Test
    fun `despido improcedente 33 dias por anio`() {
        val result = SeveranceCalculator.calculate(
            36_500.0,
            LocalDate.of(2020, 1, 1),
            LocalDate.of(2025, 1, 1),
            DismissalType.UNFAIR,
            pendingVacationDays = 10,
        )

        val years = 1827 / 365.0
        assertEquals(100.0 * 33 * years, result.compensation, 1e-6)
        assertEquals(1_000.0, result.vacationPay, 1e-9)
    }

    @Test
    fun `la indemnizacion del despido objetivo tiene tope de 12 mensualidades`() {
        val result = SeveranceCalculator.calculate(
            36_500.0,
            LocalDate.of(1990, 1, 1),
            LocalDate.of(2025, 1, 1),
            DismissalType.OBJECTIVE,
            pendingVacationDays = 0,
        )

        assertEquals(36_500.0, result.compensation, 1e-9)
    }
}
