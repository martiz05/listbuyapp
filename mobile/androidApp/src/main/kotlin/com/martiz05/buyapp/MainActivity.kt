package com.martiz05.buyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = AndroidBuyAppContainer(
            context = applicationContext,
            apiBaseUrl = BuildConfig.BUYAPP_API_BASE_URL,
        )
        setContent {
            App(container.controller)
        }
    }
}
