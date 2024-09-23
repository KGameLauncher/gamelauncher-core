package de.dasbabypixel.gamelauncher.api.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ColorTest {
    @Test
    fun testBitHandling() {
        val step = 7
        for (red in 0u until 0xFFu step step) {
            for (green in 0u until 0xFFu step step) {
                for (blue in 0u until 0xFFu step step) {
                    for (alpha in 0u until 0xFFu step step) {
                        val color = Color(red, green, blue, alpha)
                        assertEquals(color.red, red.toUByte())
                        assertEquals(color.green, green.toUByte())
                        assertEquals(color.blue, blue.toUByte())
                        assertEquals(color.alpha, alpha.toUByte())
                        assertEquals(color.ired, red)
                        assertEquals(color.igreen, green)
                        assertEquals(color.iblue, blue)
                        assertEquals(color.ialpha, alpha)
                    }
                }
            }
        }
    }

    @Test
    fun testWith() {
        val color = Color(100, 255, 10, 5)

        assertNotEquals(color withRed 50u, color)
        assertEquals(color withRed 50 withRed 100u, color)
        assertEquals(color withGreen 10u, Color(100, 10, 10, 5))
        assertEquals(color withGreen 50 withRed 10, Color(10, 50, 10, 5))
        assertEquals(color withAlpha 9, Color(100, 255, 10, 9))
        assertEquals(color.withRG(50, 10), color withRed 50 withGreen 10)
        assertEquals(color.withGB(20, 30), color withGreen 20 withBlue 30)
        assertEquals(color.withRB(90, 80), color withBlue 80 withRed 90)
        assertEquals(color.withRGB(8, 8, 8), color withRed 8 withBlue 8 withGreen 8)
        assertEquals(color.fgreen, 1F)
        assertEquals(color.fred, 100F / 255F)

        println(color::class.java)
    }
}