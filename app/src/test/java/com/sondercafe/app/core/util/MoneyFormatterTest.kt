package com.sondercafe.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun `formats zero`() {
        assertEquals("£0.00", MoneyFormatter.format(0))
    }

    @Test
    fun `formats pounds and pence`() {
        assertEquals("£3.50", MoneyFormatter.format(350))
    }

    @Test
    fun `pads single digit pence`() {
        assertEquals("£0.05", MoneyFormatter.format(5))
        assertEquals("£2.07", MoneyFormatter.format(207))
    }

    @Test
    fun `groups thousands`() {
        assertEquals("£1,234.56", MoneyFormatter.format(123_456))
    }

    @Test
    fun `formats negative amounts with a leading minus`() {
        assertEquals("-£2.50", MoneyFormatter.format(-250))
    }

    @Test
    fun `handles the smallest int without overflowing`() {
        assertEquals("-£21,474,836.48", MoneyFormatter.format(Int.MIN_VALUE))
    }
}
