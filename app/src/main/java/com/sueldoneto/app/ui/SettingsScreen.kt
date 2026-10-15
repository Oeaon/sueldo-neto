package com.sueldoneto.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sueldoneto.app.BuildConfig
import com.sueldoneto.app.R

/** Ajustes comunes: Premium, privacidad de anuncios y acerca de. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isPremium: Boolean,
    showPrivacyOptions: Boolean,
    onPrivacyOptions: () -> Unit,
    onOpenPremium: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.premium_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (!isPremium) {
                SectionTitle(stringResource(R.string.premium_title))
                Text(stringResource(R.string.premium_settings_text), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onOpenPremium, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.remove_ads))
                }
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }
            if (showPrivacyOptions) {
                OutlinedButton(onClick = onPrivacyOptions) { Text(stringResource(R.string.privacy_options)) }
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }
            SectionTitle(stringResource(R.string.about_title))
            Text(stringResource(R.string.about_text), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
