package com.hopescrolling

import androidx.compose.ui.graphics.Color
import com.hopescrolling.ui.theme.Error
import com.hopescrolling.ui.theme.OledBlack
import com.hopescrolling.ui.theme.OnBackground
import com.hopescrolling.ui.theme.OnPrimary
import com.hopescrolling.ui.theme.OnSurface
import com.hopescrolling.ui.theme.OnSurfaceVariant
import com.hopescrolling.ui.theme.OutlineVariant
import com.hopescrolling.ui.theme.Primary
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeColorTest {

    @Test fun oledBlack_isFullyBlack() = assertEquals(Color(0xFF000000), OledBlack)
    @Test fun primary_isTeal() = assertEquals(Color(0xFF26C6DA), Primary)
    @Test fun onPrimary_isDarkTeal() = assertEquals(Color(0xFF00363D), OnPrimary)
    @Test fun onBackground_isLightGrey() = assertEquals(Color(0xFFE0E0E0), OnBackground)
    @Test fun onSurface_isLightGrey() = assertEquals(Color(0xFFE0E0E0), OnSurface)
    @Test fun onSurfaceVariant_isCoolGrey() = assertEquals(Color(0xFF8A9EA0), OnSurfaceVariant)
    @Test fun outlineVariant_isDarkTeal() = assertEquals(Color(0xFF0D2427), OutlineVariant)
    @Test fun error_isRed() = assertEquals(Color(0xFFCF6679), Error)
}
