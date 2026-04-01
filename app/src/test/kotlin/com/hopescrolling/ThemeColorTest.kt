package com.hopescrolling

import androidx.compose.ui.graphics.Color
import com.hopescrolling.ui.theme.OledBlack
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeColorTest {

    @Test
    fun oledBlack_isFullyBlack() {
        assertEquals(Color(0xFF000000), OledBlack)
    }
}
