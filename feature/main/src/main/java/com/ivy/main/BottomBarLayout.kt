package com.ivy.main

import androidx.compose.ui.unit.Dp

/**
 * Pure geometry for the floating pill navigation. Kept outside the composable so the layout
 * guarantees (symmetric slots, label fit, FAB room) can be unit tested without Android.
 */

/** The pill never grows wider than [PILL_WIDTH] and always keeps [PILL_SIDE_MARGIN] on both sides. */
internal fun bottomBarPillWidth(availableWidth: Dp): Dp =
    (availableWidth - PILL_SIDE_MARGIN * 2).coerceAtMost(PILL_WIDTH)

/**
 * Each of the two tabs gets the same slot. The central FAB and the pill content padding are
 * removed first, which keeps the selected capsule and the pill edges symmetric.
 */
internal fun bottomBarTabSlotWidth(
    pillWidth: Dp,
    fabSize: Dp = FAB_BUTTON_SIZE,
    contentPadding: Dp = PILL_CONTENT_HORIZONTAL_PADDING,
): Dp = (pillWidth - contentPadding * 2 - fabSize) / 2

/**
 * A label is shown only when the whole capsule (icon + gap + label) fits the slot. Otherwise the
 * tab falls back to icon only, which avoids truncated or shifted labels on narrow screens.
 */
internal fun shouldShowBottomBarLabel(requiredWidth: Dp, slotWidth: Dp): Boolean =
    requiredWidth <= slotWidth
