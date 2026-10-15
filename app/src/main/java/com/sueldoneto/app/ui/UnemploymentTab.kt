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
import com.sueldoneto.core.UnemploymentCalculator

@Composable
fun UnemploymentTab(form: UnemploymentForm, modifier: Modifier) {
    val base = parseAmount(form.monthlyBase)
    val days = form.daysContributed.toIntOrNull()
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AmountField(form.monthlyBase, { form.monthlyBase = it }, stringResource(R.string.unemployment_base))
        Note(stringResource(R.string.unemployment_base_help))
        AmountField(
            form.daysContributed,
            { form.daysContributed = it },
            stringResource(R.string.unemployment_days),
            suffix = stringResource(R.string.days_suffix),
            decimals = false,
        )
        Stepper(stringResource(R.string.children), form.children, { form.children = it })

        if (base != null && base > 0 && days != null) {
            val result = UnemploymentCalculator.calculate(base, days, form.children)
            ResultCard {
                if (!result.hasRight) {
                    Text(stringResource(R.string.unemployment_no_right), style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(
                        stringResource(R.string.unemployment_duration, result.days, result.days / 30),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    ResultRow(stringResource(R.string.unemployment_first_180), formatEuros(result.first180), bold = true)
                    if (result.days > 180) {
                        ResultRow(stringResource(R.string.unemployment_from_181), formatEuros(result.from181))
                    }
                    if (result.days > 360) {
                        ResultRow(stringResource(R.string.unemployment_from_361), formatEuros(result.from361))
                    }
                }
            }
        }
        Note(stringResource(R.string.unemployment_note))
    }
}
