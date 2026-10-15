package com.sueldoneto.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sueldoneto.app.R
import com.sueldoneto.app.ui.theme.BreakdownColors
import com.sueldoneto.core.ContractType
import com.sueldoneto.core.SalaryCalculator
import com.sueldoneto.core.SalaryInput
import com.sueldoneto.core.SalaryResult
import com.sueldoneto.core.TaxYears

@Composable
fun SalaryTab(form: SalaryForm, modifier: Modifier) {
    val gross = parseAmount(form.gross)
    val result = gross?.takeIf { it > 0 }?.let {
        SalaryCalculator.calculate(
            SalaryInput(it, form.payments, form.contract, form.children, form.childrenUnder3),
        )
    }
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AmountField(form.gross, { form.gross = it }, stringResource(R.string.gross_annual))
        PaymentsChoice(form.payments) { form.payments = it }
        Choice(
            listOf(
                ContractType.PERMANENT to stringResource(R.string.contract_permanent),
                ContractType.TEMPORARY to stringResource(R.string.contract_temporary),
            ),
            form.contract,
        ) { form.contract = it }
        Stepper(stringResource(R.string.children), form.children, { value ->
            form.children = value
            form.childrenUnder3 = form.childrenUnder3.coerceAtMost(value)
        })
        if (form.children > 0) {
            Stepper(stringResource(R.string.children_under_3), form.childrenUnder3, { form.childrenUnder3 = it }, max = form.children)
        }
        if (result != null) SalaryResultCard(result)
        Note(stringResource(R.string.salary_note, TaxYears.current.year))
    }
}

@Composable
fun PaymentsChoice(payments: Int, onChange: (Int) -> Unit) {
    Choice(
        listOf(14 to stringResource(R.string.payments_14), 12 to stringResource(R.string.payments_12)),
        payments,
        onChange,
    )
}

@Composable
private fun SalaryResultCard(result: SalaryResult) {
    ResultCard {
        Text(stringResource(R.string.net_monthly), style = MaterialTheme.typography.titleSmall)
        Text(
            formatEuros(result.netMonthly),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        result.netExtraPay?.let { Text(stringResource(R.string.net_extra_pay, formatEuros(it))) }
        StackedBar(
            listOf(
                result.netAnnual to BreakdownColors.net,
                result.socialSecurity to BreakdownColors.socialSecurity,
                result.withholding to BreakdownColors.withholding,
            ),
        )
        ResultRow(stringResource(R.string.net_annual), formatEuros(result.netAnnual), BreakdownColors.net, bold = true)
        ResultRow(stringResource(R.string.social_security), formatEuros(result.socialSecurity), BreakdownColors.socialSecurity)
        ResultRow(
            stringResource(R.string.withholding, formatPercent(result.withholdingRate)),
            formatEuros(result.withholding),
            BreakdownColors.withholding,
        )
    }
}
