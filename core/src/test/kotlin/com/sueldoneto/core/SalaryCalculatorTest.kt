package com.sueldoneto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SalaryCalculatorTest {
    private val tax = TaxYears.Y2026

    @Test
    fun `30000 brutos en 14 pagas sin hijos`() {
        val result = SalaryCalculator.calculate(SalaryInput(30_000.0, payments = 14))

        // 6,50 % de Seguridad Social: 1950 €.
        assertEquals(1_950.0, result.socialSecurity, 0.01)
        // Base 26.050 → cuota 5.980,50 − 1.054,50 del mínimo personal = 4.926 → 16,42 %.
        assertEquals(16.42, result.withholdingRate, 0.0)
        assertEquals(4_926.0, result.withholding, 0.01)
        assertEquals(23_124.0, result.netAnnual, 0.01)
        assertEquals(30_000.0 / 14 - 1_950.0 / 12 - 4_926.0 / 14, result.netMonthly, 0.01)
        assertEquals(30_000.0 / 14 * (1 - 0.1642), result.netExtraPay!!, 0.01)
    }

    @Test
    fun `con 12 pagas el neto mensual es el anual entre 12`() {
        val result = SalaryCalculator.calculate(SalaryInput(30_000.0, payments = 12))

        assertEquals(result.netAnnual / 12, result.netMonthly, 0.01)
        assertNull(result.netExtraPay)
    }

    @Test
    fun `por debajo del limite no hay retencion`() {
        val result = SalaryCalculator.calculate(SalaryInput(15_500.0))

        assertEquals(0.0, result.withholdingRate, 0.0)
    }

    @Test
    fun `sueldo bajo con reduccion por rendimientos del trabajo`() {
        // 16.000: SS 1.040, rendimiento neto 12.960, reducción 7.302 → base 5.658 → 0,19 × 108 = 20,52 € → 0,13 %.
        val result = SalaryCalculator.calculate(SalaryInput(16_000.0))

        assertEquals(0.13, result.withholdingRate, 0.0)
    }

    @Test
    fun `el contrato temporal cotiza mas por desempleo`() {
        val permanent = SalaryCalculator.calculate(SalaryInput(25_000.0, contract = ContractType.PERMANENT))
        val temporary = SalaryCalculator.calculate(SalaryInput(25_000.0, contract = ContractType.TEMPORARY))

        assertEquals(25_000.0 * 0.0005, temporary.socialSecurity - permanent.socialSecurity, 0.01)
    }

    @Test
    fun `los hijos bajan la retencion`() {
        val none = SalaryCalculator.calculate(SalaryInput(30_000.0))
        val two = SalaryCalculator.calculate(SalaryInput(30_000.0, children = 2, childrenUnder3 = 1))

        assertTrue(two.withholdingRate < none.withholdingRate)
        // Mínimo: 5.550 + (2.400 + 2.700 + 2.800) / 2 = 9.500.
        val expectedQuota = SalaryCalculator.scale(26_050.0, tax) - SalaryCalculator.scale(9_500.0, tax)
        assertEquals(Math.round(expectedQuota / 30_000 * 10_000) / 100.0, two.withholdingRate, 0.0)
    }

    @Test
    fun `por encima de la base maxima cotiza la base maxima mas la solidaridad`() {
        val gross = 100_000.0
        val result = SalaryCalculator.calculate(SalaryInput(gross))
        val excess = gross / 12 - tax.maxMonthlyBase
        // El exceso (3.232 €/mes) pasa del 50 % de la base máxima, así que toca los tres tramos.
        val tramo1 = tax.maxMonthlyBase * 0.10
        val tramo2 = tax.maxMonthlyBase * 0.50
        val solidarity = (tramo1 * 0.0115 + (tramo2 - tramo1) * 0.0125 + (excess - tramo2) * 0.0146) *
            tax.solidarityWorkerShare * 12

        assertEquals(tax.maxMonthlyBase * 12 * tax.socialSecurityRatePermanent + solidarity, result.socialSecurity, 0.01)
    }

    @Test
    fun `escala de retenciones por tramos`() {
        assertEquals(12_450 * 0.19, SalaryCalculator.scale(12_450.0, tax), 1e-9)
        assertEquals(2_365.5 + 7_750 * 0.24 + 15_000 * 0.30 + 24_800 * 0.37 + 40_000 * 0.45, SalaryCalculator.scale(100_000.0, tax), 1e-6)
    }

    @Test
    fun `reduccion por rendimientos del trabajo en sus tramos`() {
        assertEquals(7_302.0, SalaryCalculator.workIncomeReduction(14_000.0), 1e-9)
        assertEquals(7_302.0 - 1.75 * 1_000, SalaryCalculator.workIncomeReduction(15_852.0), 1e-9)
        assertEquals(0.0, SalaryCalculator.workIncomeReduction(20_000.0), 1e-9)
    }

    @Test
    fun `la comparacion da la diferencia neta y cuanto queda de cada euro`() {
        val a = SalaryCalculator.calculate(SalaryInput(30_000.0))
        val b = SalaryCalculator.calculate(SalaryInput(35_000.0))
        val comparison = SalaryComparison(a, b)

        assertEquals(b.netAnnual - a.netAnnual, comparison.netAnnualDifference, 1e-9)
        assertTrue(comparison.keptPerExtraEuro!! in 0.5..0.75)
    }
}
