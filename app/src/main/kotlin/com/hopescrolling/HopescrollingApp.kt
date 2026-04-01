package com.hopescrolling

import androidx.compose.runtime.Composable
import com.hopescrolling.ui.navigation.AppNavigation
import com.hopescrolling.ui.theme.HopescrollingTheme

@Composable
fun HopescrollingApp() {
    HopescrollingTheme {
        AppNavigation()
    }
}
