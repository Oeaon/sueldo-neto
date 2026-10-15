package com.sueldoneto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

fun formatEuros(eur: Double): String = String.format(Locale.getDefault(), "%,.2f €", eur)

fun formatPercent(percent: Double): String = String.format(Locale.getDefault(), "%.2f %%", percent)

/**
 * Texto de un campo numérico → número. La coma es decimal ("1500,50"); el punto se toma como
 * separador de miles salvo que vaya seguido de una o dos cifras al final ("1500.5").
 */
fun parseAmount(text: String): Double? {
    val normalized = when {
        ',' in text -> text.replace(".", "").replace(',', '.')
        Regex("""\.\d{1,2}$""").containsMatchIn(text) && text.count { it == '.' } == 1 -> text
        else -> text.replace(".", "")
    }
    return normalized.toDoubleOrNull()
}

@Composable
fun AmountField(value: String, onValueChange: (String) -> Unit, label: String, suffix: String = "€", decimals: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = { text ->
            onValueChange(text.filter { it.isDigit() || (decimals && (it == ',' || it == '.')) }.take(12))
        },
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimals) KeyboardType.Decimal else KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun <T> Choice(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(label) }
        }
    }
}

/** Contador de 0 a [max] con botones − y +. */
@Composable
fun Stepper(label: String, value: Int, onValueChange: (Int) -> Unit, max: Int = 10) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        FilledTonalIconButton(onClick = { onValueChange((value - 1).coerceAtLeast(0)) }, enabled = value > 0) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text("$value", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp))
        FilledTonalIconButton(onClick = { onValueChange((value + 1).coerceAtMost(max)) }, enabled = value < max) {
            Icon(Icons.Filled.Add, contentDescription = null)
        }
    }
}

@Composable
fun ResultCard(content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() }
    }
}

@Composable
fun ResultRow(label: String, value: String, color: Color? = null, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (color != null) {
            Box(Modifier.size(10.dp).background(color, CircleShape))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Barra horizontal con el reparto del bruto. */
@Composable
fun StackedBar(parts: List<Pair<Double, Color>>) {
    val total = parts.sumOf { it.first }.takeIf { it > 0 } ?: return
    Row(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp)),
    ) {
        parts.filter { it.first > 0 }.forEach { (value, color) ->
            Box(Modifier.weight((value / total).toFloat()).height(14.dp).background(color))
        }
    }
}

@Composable
fun Note(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
