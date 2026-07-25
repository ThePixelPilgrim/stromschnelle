package de.nereide.stromschnelle.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.nereide.stromschnelle.ui.common.PriorityDimension
import de.nereide.stromschnelle.ui.theme.PriorityPalette

/**
 * A priority chip for the widget.
 *
 * IMPORTANT — in Glance the padding *is* the touch target. Glance compiles to
 * `RemoteViews`, where the clickable area is exactly the bounds of the view
 * carrying the click; there is no invisible overhang as in Compose. The outer
 * [Box] therefore sizes the target and the click sits on it, with the visible
 * 22 dp chip centred inside.
 *
 * 40 dp is below the 48 dp Material guideline. That is deliberate: at a 4x2
 * widget (~300 dp) three 48 dp targets would leave roughly 100 dp of title,
 * and Android's own home-screen widgets go smaller for the same reason.
 */
@Composable
fun WidgetPriorityBadge(
    value: Int,
    dimension: PriorityDimension,
    onClick: Action
) {
    val colors = when (dimension) {
        PriorityDimension.IMPORTANCE -> PriorityPalette.importance(value, dark = false)
        PriorityDimension.EFFORT -> PriorityPalette.effort(value, dark = false)
    }
    val darkColors = when (dimension) {
        PriorityDimension.IMPORTANCE -> PriorityPalette.importance(value, dark = true)
        PriorityDimension.EFFORT -> PriorityPalette.effort(value, dark = true)
    }

    Box(
        modifier = GlanceModifier.size(40.dp).clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = GlanceModifier
                .size(22.dp)
                .cornerRadius(6.dp)
                .background(ColorProvider(day = colors.background, night = darkColors.background)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = TextStyle(
                    color = ColorProvider(day = colors.foreground, night = darkColors.foreground),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
