package com.riva.watchtower

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.riva.watchtower.presentation.navigation.AppDestinations
import com.riva.watchtower.presentation.navigation.AppNavHost
import com.riva.watchtower.presentation.theme.WatchTowerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WatchTowerTheme {
                AppNavHost(startDestination = AppDestinations.Home)
            }
        }
    }
}