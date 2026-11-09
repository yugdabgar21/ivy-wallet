package com.ivy.ui.effects

import io.kotest.matchers.shouldBe
import org.junit.Test

class VisualEffectsIntensityTest {

    @Test
    fun `clamp keeps values inside the supported range`() {
        VisualEffectsIntensity.clamp(-40) shouldBe VisualEffectsIntensity.MIN
        VisualEffectsIntensity.clamp(0) shouldBe 0
        VisualEffectsIntensity.clamp(60) shouldBe 60
        VisualEffectsIntensity.clamp(100) shouldBe 100
        VisualEffectsIntensity.clamp(180) shouldBe VisualEffectsIntensity.MAX
    }

    @Test
    fun `fraction maps the range to zero and one`() {
        VisualEffectsIntensity.fraction(0) shouldBe 0f
        VisualEffectsIntensity.fraction(50) shouldBe 0.5f
        VisualEffectsIntensity.fraction(100) shouldBe 1f
        VisualEffectsIntensity.fraction(250) shouldBe 1f
    }

    @Test
    fun `intensity zero disables every visual addition`() {
        VisualEffectsIntensity.overlayAlpha(0) shouldBe 0f
        VisualEffectsIntensity.borderAlpha(0) shouldBe 0f
        VisualEffectsIntensity.shadowElevationDp(0) shouldBe 0
    }

    @Test
    fun `visual strength grows with the intensity`() {
        val samples = listOf(0, 25, 50, 75, 100)

        samples.zipWithNext().forEach { (lower, higher) ->
            (VisualEffectsIntensity.overlayAlpha(higher) >
                VisualEffectsIntensity.overlayAlpha(lower)) shouldBe true
            (VisualEffectsIntensity.borderAlpha(higher) >
                VisualEffectsIntensity.borderAlpha(lower)) shouldBe true
            (VisualEffectsIntensity.shadowElevationDp(higher) >=
                VisualEffectsIntensity.shadowElevationDp(lower)) shouldBe true
        }
    }

    @Test
    fun `default intensity is inside the supported range`() {
        VisualEffectsIntensity.DEFAULT shouldBe 60
        VisualEffectsIntensity.clamp(VisualEffectsIntensity.DEFAULT) shouldBe
            VisualEffectsIntensity.DEFAULT
    }
}
