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
import com.sueldoneto.app.monetization.LockedFeature
import com.sueldoneto.app.monetization.Monetization
import com.sueldoneto.app.monetization.MonetizationState
import com.sueldoneto.app.ui.theme.BreakdownColors
import com.sueldoneto.core.SalaryCalculator
import com.sueldoneto.core.SalaryComparison
import com.sueldoneto.core.SalaryInput
import kotlin.math.abs
import kotlin.math.roundToInt

/** Función Premium: comparar dos ofertas en neto. */
@Composable
fun CompareTab(
    form: CompareForm,
    monetization: Monetization,
    monetizationState: MonetizationState,
    onGoPremium: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LockedFeature(
            state = monetizationState,
            monetization = monetization,
            title = stringResource(R.string.compare_title),
            description = stringResource(R.string.compare_locked),
            onGoPremium = onGoPremium,
        ) {
            CompareContent(form)
        }
    }
}

@Composable
private fun CompareContent(form: CompareForm) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.compare_title), style = MaterialTheme.typography.titleMedium)
        AmountField(form.grossA, { form.grossA = it }, stringResource(R.string.offer_a))
        AmountField(form.grossB, { form.grossB = it }, stringResource(R.string.offer_b))
        PaymentsChoice(form.payments) { form.payments = it }
        Stepper(stringResource(R.string.children), form.children, { form.children = it })

        val a = parseAmount(form.grossA)?.takeIf { it > 0 }
        val b = parseAmount(form.grossB)?.takeIf { it > 0 }
        if (a != null && b != null) {
            val comparison = SalaryComparison(
                SalaryCalculator.calculate(SalaryInput(a, form.payments, children = form.children)),
                SalaryCalculator.calculate(SalaryInput(b, form.payments, children = form.children)),
            )
            ResultCard {
                ResultRow(stringResource(R.string.offer_a_net), formatEuros(comparison.a.netMonthly))
                ResultRow(stringResource(R.string.offer_b_net), formatEuros(comparison.b.netMonthly))
                val diff = comparison.netAnnualDifference
                val better = if (diff >= 0) R.string.offer_b else R.string.offer_a
                Text(
                    stringResource(
                        R.string.compare_difference,
                        stringResource(better),
                        formatEuros(abs(diff) / 12),
                        formatEuros(abs(diff)),
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = BreakdownColors.net,
                )
                comparison.keptPerExtraEuro?.let { kept ->
                    Note(stringResource(R.string.compare_kept, (kept * 100).roundToInt()))
                }
            }
        }
    }
}
