package de.nereide.stromschnelle.ui.theme

import androidx.compose.ui.graphics.Color
import de.nereide.stromschnelle.data.PriorityRange

/** Background and foreground for one priority badge. */
data class BadgeColors(val background: Color, val foreground: Color)

/**
 * The two priority scales.
 *
 * Hue identifies the dimension (importance is warm, effort is blue) and
 * intensity encodes the value, so the two sets cannot overlap by construction.
 * Warm-versus-blue rather than red-versus-green: red-green is the most common
 * colour vision deficiency, and these are the two axes that must never be
 * confused.
 *
 * The ramps invert between themes — on light surfaces the 3 is the darkest
 * colour, on dark surfaces the lightest; in both cases the highest contrast
 * against the background. That is why there are two sets and not one.
 *
 * Deliberately NOT part of the Material 3 [androidx.compose.material3.ColorScheme]:
 * these are semantic and must not drift with the user's dynamic colour. Both the
 * Compose badge and the Glance badge read this file so they cannot diverge.
 */
object PriorityPalette {

    private val importanceLight = listOf(
        BadgeColors(Color(0xFFFBE0DE), Color(0xFF8C1D18)),
        BadgeColors(Color(0xFFF9A825), Color(0xFF3D2800)),
        BadgeColors(Color(0xFFC62828), Color(0xFFFFFFFF))
    )

    private val importanceDark = listOf(
        BadgeColors(Color(0xFF4A2320), Color(0xFFF2B8B5)),
        BadgeColors(Color(0xFFFFB74D), Color(0xFF3D2800)),
        BadgeColors(Color(0xFFEF5350), Color(0xFF2B0B0A))
    )

    private val effortLight = listOf(
        BadgeColors(Color(0xFFCFE6FA), Color(0xFF0D3C61)),
        BadgeColors(Color(0xFF4A90D9), Color(0xFFFFFFFF)),
        BadgeColors(Color(0xFF14396B), Color(0xFFFFFFFF))
    )

    private val effortDark = listOf(
        BadgeColors(Color(0xFF1B3A52), Color(0xFF9CCDF2)),
        BadgeColors(Color(0xFF5BA3E8), Color(0xFF06243F)),
        BadgeColors(Color(0xFF8AB9F0), Color(0xFF06243F))
    )

    fun importance(value: Int, dark: Boolean): BadgeColors =
        (if (dark) importanceDark else importanceLight)[indexOf(value)]

    fun effort(value: Int, dark: Boolean): BadgeColors =
        (if (dark) effortDark else effortLight)[indexOf(value)]

    private fun indexOf(value: Int): Int =
        value.coerceIn(PriorityRange.MIN, PriorityRange.MAX) - 1
}
