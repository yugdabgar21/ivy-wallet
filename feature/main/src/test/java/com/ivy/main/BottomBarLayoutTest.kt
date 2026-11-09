package com.ivy.main

import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.Test

class BottomBarLayoutTest {

    @Test
    fun `pill keeps the side margins and never exceeds the max width`() {
        bottomBarPillWidth(availableWidth = 360.dp) shouldBe 312.dp
        bottomBarPillWidth(availableWidth = 500.dp) shouldBe PILL_WIDTH
        bottomBarPillWidth(availableWidth = 320.dp) shouldBe 288.dp
    }

    @Test
    fun `both tab slots are symmetric and leave room for the FAB`() {
        val pillWidth = bottomBarPillWidth(360.dp)

        val slot = bottomBarTabSlotWidth(pillWidth)

        slot shouldBe 122.dp
        // 2 slots + FAB + content padding reconstruct the pill width exactly.
        (slot * 2 + FAB_BUTTON_SIZE + PILL_CONTENT_HORIZONTAL_PADDING * 2) shouldBe pillWidth
    }

    @Test
    fun `slot width stays positive on a narrow phone`() {
        val slot = bottomBarTabSlotWidth(bottomBarPillWidth(280.dp))
        (slot > 0.dp) shouldBe true
    }

    @Test
    fun `label hides when it does not fit the slot`() {
        shouldShowBottomBarLabel(requiredWidth = 100.dp, slotWidth = 122.dp) shouldBe true
        shouldShowBottomBarLabel(requiredWidth = 122.dp, slotWidth = 122.dp) shouldBe true
        shouldShowBottomBarLabel(requiredWidth = 124.dp, slotWidth = 122.dp) shouldBe false
    }
}
