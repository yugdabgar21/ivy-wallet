package com.ivy.wallet.ui.theme.glass

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.ivy.design.l0_system.UI
import com.ivy.ui.effects.VisualEffectsIntensity

/**
 * Shared, dependency-free "liquid glass" styling.
 *
 * This is intentionally NOT real backdrop blur. Android's Compose `Modifier.blur` and
 * `RenderEffect` only affect a composable's own content, not what is behind it. What we call
 * glass here is a translucent surface plus a soft gradient sheen, a thin highlight border and a
 * restrained shadow. All values scale with the user's intensity setting and 0 means "no extra
 * styling", so turning the effect off or to 0 restores the original design.
 */

@Composable
fun glassOverlayBrush(intensity: Int): Brush {
    val fraction = VisualEffectsIntensity.fraction(intensity)
    val tint = UI.colors.pureInverse

    return Brush.verticalGradient(
        colors = listOf(
            tint.copy(alpha = 0.05f * fraction),
            Color.Transparent,
            tint.copy(alpha = 0.08f * fraction),
        )
    )
}

@Composable
fun glassScrimBrush(intensity: Int): Brush {
    val fraction = VisualEffectsIntensity.fraction(intensity)
    val base = UI.colors.medium

    return Brush.verticalGradient(
        colors = listOf(
            base.copy(alpha = 0.95f),
            base.copy(alpha = 0.95f - 0.18f * fraction),
        )
    )
}

/**
 * A hairline that starts at [base] and fades towards [highlight] as the intensity grows.
 * Passing a transparent base keeps the component unchanged at intensity 0.
 */
@Composable
fun glassBorderColor(
    intensity: Int,
    base: Color = Color.Transparent,
    highlight: Color? = null,
): Color {
    val resolvedHighlight = highlight ?: UI.colors.pureInverse.copy(alpha = 0.30f)
    return lerp(base, resolvedHighlight, VisualEffectsIntensity.fraction(intensity))
}

/**
 * A thin "highlight" border: brighter at the top, fading towards the bottom. This is the border
 * used by the floating navigation pill, and it scales with [intensity].
 */
@Composable
fun glassBorderBrush(
    intensity: Int,
    base: Color = Color.Transparent,
    highlight: Color? = null,
): Brush {
    val fraction = VisualEffectsIntensity.fraction(intensity)
    val resolvedHighlight = highlight ?: UI.colors.pureInverse.copy(alpha = 0.45f)
    val top = lerp(base, resolvedHighlight, fraction)
    val bottom = lerp(base, resolvedHighlight.copy(alpha = resolvedHighlight.alpha * 0.20f), fraction)
    return Brush.verticalGradient(colors = listOf(top, bottom))
}
