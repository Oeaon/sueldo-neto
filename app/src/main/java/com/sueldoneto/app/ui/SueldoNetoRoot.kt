package com.sueldoneto.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sueldoneto.app.R
import com.sueldoneto.app.monetization.AdBanner
import com.sueldoneto.app.monetization.Monetization
import com.sueldoneto.app.monetization.MonetizationState
import com.sueldoneto.app.monetization.PremiumAction
import com.sueldoneto.app.monetization.PremiumScreen

private enum class Screen { MAIN, SETTINGS, PREMIUM }

enum class CalcTab(val label: Int) {
    SALARY(R.string.tab_salary),
    COMPARE(R.string.tab_compare),
    UNEMPLOYMENT(R.string.tab_unemployment),
    SEVERANCE(R.string.tab_severance),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SueldoNetoRoot(monetization: Monetization, monetizationState: MonetizationState) {
    val activity = LocalActivity.current
    var screen by rememberSaveable { mutableStateOf(Screen.MAIN) }
    var tab by rememberSaveable { mutableStateOf(CalcTab.SALARY) }
    // Las pestañas guardan lo escrito aunque se cambie de una a otra.
    val forms = remember { Forms() }

    when (screen) {
        Screen.PREMIUM -> {
            BackHandler { screen = Screen.MAIN }
            PremiumScreen(
                state = monetizationState,
                monetization = monetization,
                features = listOf(stringResource(R.string.premium_feature_compare)),
                onBack = { screen = Screen.MAIN },
            )
        }
        Screen.SETTINGS -> {
            BackHandler { screen = Screen.MAIN }
            SettingsScreen(
                isPremium = monetizationState.isPremium,
                showPrivacyOptions = remember { activity?.let(monetization::isPrivacyOptionsRequired) ?: false },
                onPrivacyOptions = { activity?.let(monetization::showPrivacyOptions) },
                onOpenPremium = { screen = Screen.PREMIUM },
                onBack = { screen = Screen.MAIN },
            )
        }
        Screen.MAIN -> Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name)) },
                        actions = {
                            PremiumAction(monetizationState) { screen = Screen.PREMIUM }
                            IconButton(onClick = { screen = Screen.SETTINGS }) {
                                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                            }
                        },
                    )
                    PrimaryScrollableTabRow(selectedTabIndex = tab.ordinal) {
                        CalcTab.entries.forEach { option ->
                            Tab(
                                selected = option == tab,
                                onClick = {
                                    if (option != tab) {
                                        tab = option
                                        // Cambiar de calculadora es una pausa natural (con límite de frecuencia).
                                        activity?.let { monetization.maybeShowInterstitial(it) }
                                    }
                                },
                                text = { Text(stringResource(option.label)) },
                            )
                        }
                    }
                }
            },
            bottomBar = { AdBanner(monetizationState) },
        ) { padding ->
            val modifier = Modifier.padding(padding).fillMaxSize()
            when (tab) {
                CalcTab.SALARY -> SalaryTab(forms.salary, modifier)
                CalcTab.COMPARE -> CompareTab(forms.compare, monetization, monetizationState, { screen = Screen.PREMIUM }, modifier)
                CalcTab.UNEMPLOYMENT -> UnemploymentTab(forms.unemployment, modifier)
                CalcTab.SEVERANCE -> SeveranceTab(forms.severance, modifier)
            }
        }
    }
}
