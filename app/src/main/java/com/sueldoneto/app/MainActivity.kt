package com.sueldoneto.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sueldoneto.app.ui.SueldoNetoRoot
import com.sueldoneto.app.ui.theme.SueldoNetoTheme

class MainActivity : ComponentActivity() {
    private val monetization by lazy { (application as SueldoNetoApp).monetization }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        monetization.gatherConsent(this)

        setContent {
            SueldoNetoTheme {
                val monetizationState by monetization.state.collectAsStateWithLifecycle()
                SueldoNetoRoot(monetization, monetizationState)
            }
        }
    }
}
