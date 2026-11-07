/*
   Copyright 2025 Kyant

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ivy.legacy.ui.liquidglass

import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * True "liquid glass" backdrop implemented on top of [android.graphics.RenderNode].
 *
 * Compose 1.6.8 does not expose `GraphicsLayer`/`rememberGraphicsLayer` (that API arrived in
 * Compose 1.7), so the kyant0/AndroidLiquidGlass capture layer cannot be used as-is. This file
 * keeps the same idea: the screen content is recorded once into an offscreen [RenderNode], and
 * the glass surface samples that node through a [RenderEffect] blur (Android 12+) plus an AGSL
 * refraction shader (Android 13+).
 *
 * Recording only happens on a hardware accelerated canvas. Previews, Paparazzi and software
 * rendering fall back to the normal translucent styling.
 *
 * Only the AGSL shader string is vendored from Kyant0/AndroidLiquidGlass (Apache-2.0). See
 * `NOTICE.md` and `LICENSE-APACHE-2.0.txt` in this directory.
 */

/** Tunable glass parameters. Kept public so the bottom bar can expose them. */
object LiquidGlassDefaults {
    /** Default blur radius. The UI scales this by the user's intensity setting. */
    val BlurRadius: Dp = 20.dp

    /** Refraction band height used by the vendored shader. */
    val RefractionHeight: Dp = 23.dp

    /** Refraction displacement used by the vendored shader. */
    val RefractionAmount: Dp = 64.dp

    /** Corner radius used by the floating pill. Equals half of its height. */
    val CornerRadius: Dp = 32.dp
}

@Stable
class LiquidGlassBackdrop internal constructor() {

    /**
     * Render node state. Only created on Android 12+, so older devices never load the
     * `RenderNode`/`RenderEffect` classes at all.
     */
    internal var nodes: GlassNodes? = null

    internal var contentBounds: Rect = Rect.Zero
    internal var panelBounds: Rect = Rect.Zero

    /**
     * Bumped every time the source content is re-recorded. Reading it from the glass draw
     * registers a snapshot read, so the (small) glass layer is redrawn whenever the content
     * changes. The pill is clipped into its own graphics layer, so this only invalidates the
     * glass layer and not the whole screen.
     */
    internal var frame by mutableIntStateOf(0)

    internal var cachedEffect: CachedEffect? = null

    internal fun release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            nodes?.release()
        }
        nodes = null
        contentBounds = Rect.Zero
        panelBounds = Rect.Zero
        cachedEffect = null
    }
}

/** Holds the [RenderNode]s used to capture and blur the backdrop. Android 12+ only. */
@RequiresApi(Build.VERSION_CODES.S)
internal class GlassNodes {
    internal var contentNode: RenderNode? = null
    internal var effectNode: RenderNode? = null

    internal fun release() {
        contentNode?.discardDisplayList()
        effectNode?.discardDisplayList()
        contentNode = null
        effectNode = null
    }
}

internal data class CachedEffect(
    val blurRadiusPx: Float,
    val width: Int,
    val height: Int,
    val refraction: Boolean,
    val intensity: Float,
    val cornerRadiusPx: Float,
    val refractionHeightPx: Float,
    val refractionAmountPx: Float,
    val effect: RenderEffect?,
)

@Composable
fun rememberLiquidGlassBackdrop(): LiquidGlassBackdrop {
    val backdrop = remember { LiquidGlassBackdrop() }
    DisposableEffect(backdrop) {
        onDispose { backdrop.release() }
    }
    return backdrop
}

/**
 * Wrap the content that should be visible through the glass surfaces.
 *
 * On Android 12+ the content is drawn into a [RenderNode] and then blitted to the screen, so the
 * glass surfaces can sample exactly what is behind them. On older versions (and on software
 * canvases) it simply draws the content as usual.
 */
fun Modifier.liquidGlassSource(backdrop: LiquidGlassBackdrop): Modifier =
    this
        .onGloballyPositioned { coordinates ->
            if (coordinates.isAttached) {
                backdrop.contentBounds = coordinates.boundsInWindow()
            }
        }
        .drawWithContent {
            val nativeCanvas = drawContext.canvas.nativeCanvas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && nativeCanvas.isHardwareAccelerated) {
                val nodes = backdrop.nodes ?: GlassNodes().also { backdrop.nodes = it }
                val node = nodes.contentNode ?: RenderNode("ivy-glass-content").also {
                    nodes.contentNode = it
                }
                val width = size.width.toInt()
                val height = size.height.toInt()
                if (width > 0 && height > 0) {
                    node.setPosition(0, 0, width, height)
                    val recording = node.beginRecording()
                    // Redirect the content draw into the RenderNode's canvas. `drawContent()`
                    // resolves the canvas through the draw context, so swapping it here is enough
                    // and avoids needing the Compose 1.7 GraphicsLayer API.
                    val context = drawContext
                    val previousCanvas = context.canvas
                    context.canvas = Canvas(recording)
                    try {
                        this@drawWithContent.drawContent()
                    } finally {
                        context.canvas = previousCanvas
                        node.endRecording()
                    }
                    nativeCanvas.drawRenderNode(node)
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
                        // API 31 does not always pick up a fresh display list on its own, so the
                        // glass surfaces have to be re-drawn whenever the source re-records.
                        backdrop.frame++
                    }
                } else {
                    drawContent()
                }
            } else {
                drawContent()
            }
        }

/**
 * Draws the blurred, optionally refracted backdrop behind the receiver's content.
 *
 * The receiver's size defines the glass surface size; the content is clipped to [shape].
 */
fun Modifier.liquidGlassPanel(
    backdrop: LiquidGlassBackdrop,
    shape: Shape,
    cornerRadius: Dp = LiquidGlassDefaults.CornerRadius,
    blurRadius: Dp = LiquidGlassDefaults.BlurRadius,
    refraction: Boolean = true,
    intensity: Float = 1f,
): Modifier = this
    .onGloballyPositioned { coordinates ->
        if (coordinates.isAttached) {
            backdrop.panelBounds = coordinates.boundsInWindow()
        }
    }
    .drawWithContent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            drawLiquidGlass(
                backdrop = backdrop,
                shape = shape,
                cornerRadius = cornerRadius,
                blurRadius = blurRadius,
                refraction = refraction,
                intensity = intensity,
            )
        }
        drawContent()
    }

@RequiresApi(Build.VERSION_CODES.S)
private fun ContentDrawScope.drawLiquidGlass(
    backdrop: LiquidGlassBackdrop,
    shape: Shape,
    cornerRadius: Dp,
    blurRadius: Dp,
    refraction: Boolean,
    intensity: Float,
) {
    val nativeCanvas = drawContext.canvas.nativeCanvas
    if (!nativeCanvas.isHardwareAccelerated) return

    val content = backdrop.nodes?.contentNode ?: return
    val contentBounds = backdrop.contentBounds
    val panelBounds = backdrop.panelBounds
    if (contentBounds.isEmpty || panelBounds.isEmpty) return

    val width = size.width.toInt()
    val height = size.height.toInt()
    if (width <= 0 || height <= 0) return

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
        // Reading the counter registers a snapshot read so this draw re-runs whenever the source
        // re-records, which API 31 needs because it does not always refresh RenderNodes on its own.
        if (backdrop.frame < 0) return
    }

    val blurRadiusPx = (blurRadius * intensity.coerceIn(0f, 1f)).toPx()
    if (blurRadiusPx <= 0f) return

    val cornerRadiusPx = cornerRadius.toPx()
    val effect = backdrop.obtainEffect(
        blurRadiusPx = blurRadiusPx,
        width = width,
        height = height,
        refraction = refraction,
        intensity = intensity,
        cornerRadiusPx = cornerRadiusPx,
        refractionHeightPx = LiquidGlassDefaults.RefractionHeight.toPx(),
        refractionAmountPx = LiquidGlassDefaults.RefractionAmount.toPx(),
    )

    val nodes = backdrop.nodes ?: return
    val effectNode = nodes.effectNode ?: RenderNode("ivy-glass-effect").also {
        nodes.effectNode = it
    }

    effectNode.setPosition(0, 0, width, height)
    effectNode.setRenderEffect(effect)
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) {
        // Android 12 sometimes keeps a stale display list on the effect node. Dropping it before
        // re-recording forces the compositor to pick up the fresh blurred content.
        effectNode.discardDisplayList()
    }

    val recording = effectNode.beginRecording()
    try {
        // The content node is recorded in the source's local coordinates. Shift it so that the
        // glass surface samples the part of the content that sits behind it.
        recording.translate(
            -(panelBounds.left - contentBounds.left),
            -(panelBounds.top - contentBounds.top),
        )
        recording.drawRenderNode(content)
    } finally {
        effectNode.endRecording()
    }

    val path = Path()
    path.addOutline(shape.createOutline(size, layoutDirection, this))
    clipPath(path) {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRenderNode(effectNode)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun LiquidGlassBackdrop.obtainEffect(
    blurRadiusPx: Float,
    width: Int,
    height: Int,
    refraction: Boolean,
    intensity: Float,
    cornerRadiusPx: Float,
    refractionHeightPx: Float,
    refractionAmountPx: Float,
): RenderEffect? {
    val cached = cachedEffect
    if (
        cached != null &&
        cached.blurRadiusPx == blurRadiusPx &&
        cached.width == width &&
        cached.height == height &&
        cached.refraction == refraction &&
        cached.intensity == intensity &&
        cached.cornerRadiusPx == cornerRadiusPx &&
        cached.refractionHeightPx == refractionHeightPx &&
        cached.refractionAmountPx == refractionAmountPx
    ) {
        return cached.effect
    }

    val effect = buildEffect(
        blurRadiusPx = blurRadiusPx,
        width = width,
        height = height,
        refraction = refraction,
        intensity = intensity,
        cornerRadiusPx = cornerRadiusPx,
        refractionHeightPx = refractionHeightPx,
        refractionAmountPx = refractionAmountPx,
    )
    cachedEffect = CachedEffect(
        blurRadiusPx = blurRadiusPx,
        width = width,
        height = height,
        refraction = refraction,
        intensity = intensity,
        cornerRadiusPx = cornerRadiusPx,
        refractionHeightPx = refractionHeightPx,
        refractionAmountPx = refractionAmountPx,
        effect = effect,
    )
    return effect
}

@RequiresApi(Build.VERSION_CODES.S)
private fun buildEffect(
    blurRadiusPx: Float,
    width: Int,
    height: Int,
    refraction: Boolean,
    intensity: Float,
    cornerRadiusPx: Float,
    refractionHeightPx: Float,
    refractionAmountPx: Float,
): RenderEffect {
    val blur = RenderEffect.createBlurEffect(
        blurRadiusPx,
        blurRadiusPx,
        android.graphics.Shader.TileMode.CLAMP,
    )
    if (!refraction || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return blur
    }

    val strength = intensity.coerceIn(0f, 1f)
    val shader = RuntimeShader(RoundedRectRefractionShaderString)
    shader.setFloatUniform("size", width.toFloat(), height.toFloat())
    shader.setFloatUniform("offset", 0f, 0f)
    shader.setFloatUniform(
        "cornerRadii",
        cornerRadiusPx,
        cornerRadiusPx,
        cornerRadiusPx,
        cornerRadiusPx,
    )
    shader.setFloatUniform("refractionHeight", refractionHeightPx * strength)
    shader.setFloatUniform("refractionAmount", refractionAmountPx * strength)
    shader.setFloatUniform("depthEffect", 0f)

    val lens = RenderEffect.createRuntimeShaderEffect(shader, "content")
    return RenderEffect.createChainEffect(lens, blur)
}

/** True when the platform can render the real backdrop blur. */
fun isLiquidGlassSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** True when the platform can additionally run the AGSL refraction shader. */
fun isLiquidGlassRefractionSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
