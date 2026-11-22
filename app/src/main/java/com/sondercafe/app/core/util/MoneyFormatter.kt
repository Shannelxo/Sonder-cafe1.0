package com.sondercafe.app.core.util

import java.util.Locale
import kotlin.math.abs

/**
 * Formats prices stored as whole pence, e.g. 350 -> "£3.50".
 *
 * Money is kept as an Int number of pence everywhere in the app so totals are always exact.
 * Floating point types can't represent values like 0.10 precisely, which causes rounding errors
 * when prices are added up.
 */
object MoneyFormatter {

    fun format(pence: Int): String {
        val absolute = abs(pence.toLong())
        val pounds = absolute / 100
        val remainder = absolute % 100
        val sign = if (pence < 0) "-" else ""
        return String.format(Locale.UK, "%s£%,d.%02d", sign, pounds, remainder)
    }
}
