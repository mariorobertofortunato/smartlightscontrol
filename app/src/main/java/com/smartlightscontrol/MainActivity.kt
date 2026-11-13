package com.smartlightscontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartlightscontrol.presentation.HomeScreen
import com.smartlightscontrol.ui.theme.SmartLightsControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartLightsControlTheme {
                HomeScreen()
            }
        }
    }
}
