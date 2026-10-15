package com.sueldoneto.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sueldoneto.app.R
import com.sueldoneto.core.DismissalType
import com.sueldoneto.core.SeveranceCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun SeveranceTab(form: SeveranceForm, modifier: Modifier) {
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AmountField(form.gross, { form.gross = it }, stringResource(R.string.gross_annual))
        DateField(stringResource(R.string.start_date), form.start) { form.start = it }
        DateField(stringResource(R.string.end_date), form.end) { form.end = it }
        Column(Modifier.selectableGroup()) {
            DismissalType.entries.forEach { type ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = type == form.type, onClick = { form.type = type }, role = Role.RadioButton)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = type == form.type, onClick = null)
                    Text(dismissalName(type), Modifier.padding(start = 12.dp))
                }
            }
        }
        AmountField(
            form.vacationDays,
            { form.vacationDays = it },
            stringResource(R.string.vacation_days),
            suffix = stringResource(R.string.days_suffix),
            decimals = false,
        )

        val gross = parseAmount(form.gross)
        val start = form.start
        if (gross != null && gross > 0 && start != null && !form.end.isBefore(start)) {
            val result = SeveranceCalculator.calculate(gross, start, form.end, form.type, form.vacationDays.toIntOrNull() ?: 0)
            ResultCard {
                ResultRow(
                    stringResource(R.string.years_worked),
                    String.format(Locale.getDefault(), "%.2f", result.yearsWorked),
                )
                ResultRow(stringResource(R.string.compensation), formatEuros(result.compensation))
                ResultRow(stringResource(R.string.vacation_pay), formatEuros(result.vacationPay))
                ResultRow(stringResource(R.string.severance_total), formatEuros(result.total), bold = true)
            }
        }
        Note(stringResource(R.string.severance_note))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, date: LocalDate?, onDate: (LocalDate) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
        Text(
            if (date == null) label else "$label: ${date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}",
            fontWeight = if (date == null) FontWeight.Normal else FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    if (open) {
        // El DatePicker trabaja en milisegundos UTC.
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                    open = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text(stringResource(android.R.string.cancel)) }
            },
        ) { DatePicker(state) }
    }
}

@Composable
private fun dismissalName(type: DismissalType): String = stringResource(
    when (type) {
        DismissalType.UNFAIR -> R.string.dismissal_unfair
        DismissalType.OBJECTIVE -> R.string.dismissal_objective
        DismissalType.END_OF_TEMPORARY -> R.string.dismissal_temporary
        DismissalType.NONE -> R.string.dismissal_none
    },
)
