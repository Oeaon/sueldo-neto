package com.sueldoneto.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF4A3AA8),
    secondary = Color(0xFF5E5C71),
    tertiary = Color(0xFF7B5266),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC6BFFF),
    secondary = Color(0xFFC8C4DC),
    tertiary = Color(0xFFEDB8CF),
)

@Composable
fun SueldoNetoTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

/** Colores fijos del desglose de la nómina, legibles en claro y oscuro. */
object BreakdownColors {
    val net = Color(0xFF2E7D32)
    val socialSecurity = Color(0xFF1565C0)
    val withholding = Color(0xFFE65100)
}
