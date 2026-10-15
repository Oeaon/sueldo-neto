package com.sueldoneto.app

import android.app.Application
import com.sueldoneto.app.monetization.Monetization

class SueldoNetoApp : Application() {
    lateinit var monetization: Monetization
        private set

    override fun onCreate() {
        super.onCreate()
        monetization = Monetization(this)
    }
}
