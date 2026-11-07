package com.ivy.main

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.utils.thenIf
import com.ivy.legacy.data.model.MainTab
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.ui.liquidglass.LiquidGlassBackdrop
import com.ivy.legacy.ui.liquidglass.LiquidGlassDefaults
import com.ivy.legacy.ui.liquidglass.liquidGlassPanel
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.densityScope
import com.ivy.legacy.utils.lerp
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.springBounceFast
import com.ivy.legacy.utils.toDensityDp
import com.ivy.legacy.utils.toDensityPx
import com.ivy.ui.R
import com.ivy.ui.effects.LocalVisualEffectsStore
import com.ivy.ui.effects.VisualEffectComponent
import com.ivy.ui.effects.VisualEffectsIntensity
import com.ivy.wallet.ui.theme.Gradient
import com.ivy.wallet.ui.theme.GradientGreen
import com.ivy.wallet.ui.theme.GradientIvy
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Ivy
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.IvyCircleButton
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.IvyOutlinedButton
import com.ivy.wallet.ui.theme.gradientExpenses
import com.ivy.wallet.ui.theme.glass.glassBorderBrush
import com.ivy.wallet.ui.theme.glass.glassBorderColor
import com.ivy.wallet.ui.theme.glass.glassOverlayBrush
import com.ivy.wallet.ui.theme.modal.AddModalBackHandling
import com.ivy.wallet.ui.theme.pureBlur
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

val TRN_BUTTON_CLICK_AREA_HEIGHT = 150.dp
val FAB_BUTTON_SIZE = 56.dp

/**
 * Floating pill navigation geometry. Blur radius and corner radius are exposed here so they can be
 * tuned without touching the layout code.
 */
val PILL_WIDTH = 312.dp
val PILL_HEIGHT = 64.dp
val PILL_SIDE_MARGIN = 16.dp
val PILL_BOTTOM_MARGIN = 8.dp
val PILL_CORNER_RADIUS = LiquidGlassDefaults.CornerRadius
val PILL_BLUR_RADIUS = LiquidGlassDefaults.BlurRadius
val PILL_BORDER_WIDTH = 1.dp
val TAB_MIN_TOUCH_TARGET = 48.dp
// Fixed gap between the selected tab capsule and the central FAB, equal on both sides.
val TAB_FAB_GAP = 12.dp

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BoxWithConstraintsScope.BottomBar(
    tab: MainTab,
    selectTab: (MainTab) -> Unit,

    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTransfer: () -> Unit,
    onAddPlannedPayment: () -> Unit,

    showAddAccountModal: () -> Unit,

    backdrop: LiquidGlassBackdrop? = null,
) {
    val ivyContext = ivyWalletCtx()

    val visualEffects = LocalVisualEffectsStore.current
    val pillGlassEnabled =
        visualEffects?.componentEnabled(VisualEffectComponent.BOTTOM_NAV) == true
    val glassIntensity = visualEffects?.intensity ?: VisualEffectsIntensity.DEFAULT
    val glassFraction = VisualEffectsIntensity.fraction(glassIntensity)
    val realGlass = pillGlassEnabled && backdrop != null && glassFraction > 0f

    val pillShape = RoundedCornerShape(PILL_CORNER_RADIUS)
    val pillBaseColor = when {
        // Real backdrop blur needs a much lighter tint, otherwise the blur is invisible.
        realGlass -> UI.colors.pure.copy(alpha = 0.34f + 0.08f * glassFraction)
        pillGlassEnabled -> UI.colors.pure.copy(alpha = 0.86f)
        else -> pureBlur()
    }
    val pillOverlay = if (pillGlassEnabled) {
        glassOverlayBrush(glassIntensity)
    } else {
        SolidColor(Color.Transparent)
    }
    val pillBorderBrush = if (pillGlassEnabled) {
        glassBorderBrush(
            intensity = glassIntensity,
            base = UI.colors.medium.copy(alpha = 0.4f),
            highlight = UI.colors.pureInverse.copy(alpha = 0.45f)
        )
    } else {
        SolidColor(UI.colors.medium.copy(alpha = 0.4f))
    }
    val pillElevation = if (pillGlassEnabled) {
        (8 + VisualEffectsIntensity.shadowElevationDp(glassIntensity)).dp
    } else {
        8.dp
    }
    val glassModifier = if (realGlass && backdrop != null) {
        Modifier.liquidGlassPanel(
            backdrop = backdrop,
            shape = pillShape,
            cornerRadius = PILL_CORNER_RADIUS,
            blurRadius = PILL_BLUR_RADIUS,
            refraction = true,
            intensity = glassFraction,
        )
    } else {
        Modifier
    }
    val fabBorderColor = if (
        visualEffects?.componentEnabled(VisualEffectComponent.FAB) == true
    ) {
        glassBorderColor(
            intensity = glassIntensity,
            highlight = White.copy(alpha = 0.45f)
        )
    } else {
        null
    }

    var expanded by remember { mutableStateOf(false) }

    val modalId = remember { UUID.randomUUID() }

    AddModalBackHandling(
        modalId = modalId,
        visible = expanded
    ) {
        expanded = false
    }

    val screenHeightDp = densityScope { ivyContext.screenHeight.toDp() }
    val expandedBackgroundOffset by animateDpAsState(
        targetValue = if (expanded) 0.dp else screenHeightDp,
        animationSpec = springBounceFast()
    )

    val fabRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = springBounceFast()
    )

    val buttonsShownPercent by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = springBounceFast()
    )

    // Collapse the expanded add-menu whenever the selected tab changes.
    LaunchedEffect(tab) {
        expanded = false
    }

    // Floating pill bottom navigation.
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = PILL_BOTTOM_MARGIN)
            .padding(horizontal = PILL_SIDE_MARGIN)
            .widthIn(max = PILL_WIDTH)
            .fillMaxWidth()
            .height(PILL_HEIGHT)
            .shadow(
                elevation = pillElevation,
                shape = pillShape,
                clip = false
            )
            .clip(pillShape)
            .then(glassModifier)
            .background(pillBaseColor, pillShape)
            .thenIf(pillGlassEnabled) {
                background(pillOverlay, pillShape)
            }
            .border(
                width = PILL_BORDER_WIDTH,
                brush = pillBorderBrush,
                shape = pillShape
            )
            .alpha(1f - buttonsShownPercent)
            .clickableNoIndication(rememberInteractionSource()) {
                // consume click
            }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Tab(
            icon = R.drawable.ic_home,
            name = stringResource(R.string.home),
            selected = tab == MainTab.HOME,
            selectedColor = Ivy,
            capsuleArrangement = Arrangement.End,
            capsulePadding = PaddingValues(end = TAB_FAB_GAP),
        ) {
            selectTab(MainTab.HOME)
        }

        Spacer(Modifier.width(FAB_BUTTON_SIZE))

        Tab(
            icon = R.drawable.ic_accounts,
            name = stringResource(R.string.accounts),
            selected = tab == MainTab.ACCOUNTS,
            selectedColor = Green,
            capsuleArrangement = Arrangement.Start,
            capsulePadding = PaddingValues(start = TAB_FAB_GAP),
        ) {
            selectTab(MainTab.ACCOUNTS)
        }
    }

    if (expandedBackgroundOffset < screenHeightDp) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = expandedBackgroundOffset)
                .background(UI.colors.pure.copy(alpha = 0.95f))
                .clickableNoIndication(rememberInteractionSource()) {
                    // consume click, do nothing
                }
                .zIndex(100f)
        )
    }

    // ------------------------------------ BUTTONS--------------------------------------------------
    val fabStartX = ivyContext.screenWidth / 2 - FAB_BUTTON_SIZE.toDensityPx() / 2
    val navBarBottom = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    // Center the FAB inside the pill so it reads as one floating control.
    val fabCenterOffset = PILL_BOTTOM_MARGIN + PILL_HEIGHT / 2
    val fabStartY = ivyContext.screenHeight - navBarBottom -
            fabCenterOffset.toDensityPx() - FAB_BUTTON_SIZE.toDensityPx() / 2

    TransactionButtons(
        buttonsShownPercent = buttonsShownPercent,

        fabStartX = fabStartX,
        fabStartY = fabStartY,

        onAddIncome = onAddIncome,
        onAddExpense = onAddExpense,
        onAddTransfer = onAddTransfer,
        onAddPlannedPayment = onAddPlannedPayment
    )

    var dragOffset by remember {
        mutableStateOf(Offset.Zero)
    }
    // + & x button
    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = fabStartX.roundToInt(),
                        y = fabStartY.roundToInt()
                    )
                }
            }
            .size(FAB_BUTTON_SIZE)
            .rotate(fabRotation)
            .zIndex(200f)
            .thenIf(tab == MainTab.HOME) {
                pointerInput(Unit) {
                    detectDragGestures(
                        onDragCancel = {
                            dragOffset = Offset.Zero
                        },
                        onDragEnd = {
                            dragOffset = Offset.Zero
                        },
                        onDrag = { _, dragAmount ->
                            dragOffset += dragAmount

                            val horizontalThreshold = 40
                            val verticalThreshold = 60

                            when {
                                abs(dragOffset.x) < horizontalThreshold &&
                                        dragOffset.y < -verticalThreshold -> {
                                    // swipe up
                                    dragOffset = Offset.Zero // prevent double open of the screen
                                    onAddExpense()
                                }

                                dragOffset.x < -horizontalThreshold &&
                                        dragOffset.y < -verticalThreshold -> {
                                    // swipe up left
                                    dragOffset = Offset.Zero // prevent double open of the screen
                                    onAddIncome()
                                }

                                dragOffset.x > horizontalThreshold &&
                                        dragOffset.y < -verticalThreshold -> {
                                    // swipe up right
                                    dragOffset = Offset.Zero // prevent double open of the screen
                                    onAddTransfer()
                                }
                            }
                        }
                    )
                }
            }
            .testTag("fab_add"),
        backgroundPadding = 8.dp,
        icon = R.drawable.ic_add,
        glassBorder = fabBorderColor,
        backgroundGradient = when (tab) {
            MainTab.HOME -> {
                if (!expanded) GradientIvy else Gradient.solid(UI.colors.gray)
            }

            MainTab.ACCOUNTS -> {
                GradientGreen
            }
        },
        hasShadow = !expanded,
        tint = when (tab) {
            MainTab.HOME -> White
            MainTab.ACCOUNTS -> White
        }
    ) {
        when (tab) {
            MainTab.HOME -> {
                expanded = !expanded
            }

            MainTab.ACCOUNTS -> {
                showAddAccountModal()
            }
        }
    }
}

@Composable
private fun TransactionButtons(
    buttonsShownPercent: Float,

    fabStartX: Float,
    fabStartY: Float,

    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTransfer: () -> Unit,
    onAddPlannedPayment: () -> Unit,
) {
    val ivyContext = ivyWalletCtx()

    val bH = 48.dp
    val bV = 20.dp // 24.dp
    val bCenterV = 74.dp // 80.dp

    if (buttonsShownPercent > 0.01f) {
        val buttonLeftX = bH.toDensityPx()
        val buttonRightX = ivyContext.screenWidth - bH.toDensityPx() - FAB_BUTTON_SIZE.toDensityPx()

        val sideButtonsY = fabStartY - bV.toDensityPx() - FAB_BUTTON_SIZE.toDensityPx()
        val buttonCenterY = fabStartY - bCenterV.toDensityPx() - FAB_BUTTON_SIZE.toDensityPx()

        val clickAreaWidth = ivyContext.screenWidth / 3

        IvyOutlinedButton(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placealbe = measurable.measure(constraints)
                    layout(placealbe.width, placealbe.height) {
                        placealbe.place(
                            x = ivyContext.screenWidth / 2 - placealbe.width / 2,
                            y = buttonCenterY.roundToInt() - 48.dp.roundToPx() - placealbe.height - FAB_BUTTON_SIZE.roundToPx()
                        )
                    }
                }
                .padding(horizontal = 8.dp)
                .alpha(buttonsShownPercent)
                .zIndex(200f)
                .testTag("add_planned_payment"),
            iconStart = R.drawable.ic_planned_payments,
            text = stringResource(R.string.add_planned_payment),
            solidBackground = true
        ) {
            onAddPlannedPayment()
        }

        // Add INCOME ------------------------------------------------------------------------------
        AddIncomeButton(
            buttonsShownPercent = buttonsShownPercent,
            fabStartX = fabStartX,
            fabStartY = fabStartY,
            buttonLeftX = buttonLeftX,
            sideButtonsY = sideButtonsY,
            clickAreaWidth = clickAreaWidth,
            onAddIncome = onAddIncome
        )
        // Add INCOME ------------------------------------------------------------------------------

        // Add EXPENSE ------------------------------------------------------------------------------
        AddExpenseButton(
            buttonsShownPercent = buttonsShownPercent,
            fabStartX = fabStartX,
            fabStartY = fabStartY,
            buttonCenterY = buttonCenterY,
            clickAreaWidth = clickAreaWidth,
            onAddExpense = onAddExpense
        )
        // Add EXPENSE ------------------------------------------------------------------------------

        // Add TRANSFER ----------------------------------------------------------------------------
        AddTransferButton(
            buttonsShownPercent = buttonsShownPercent,
            fabStartX = fabStartX,
            fabStartY = fabStartY,
            buttonRightX = buttonRightX,
            sideButtonsY = sideButtonsY,
            clickAreaWidth = clickAreaWidth,
            onAddTransfer = onAddTransfer
        )
        // Add TRANSFER ----------------------------------------------------------------------------
    }
}

@Composable
private fun AddIncomeButton(
    buttonsShownPercent: Float,
    fabStartX: Float,
    fabStartY: Float,
    buttonLeftX: Float,
    sideButtonsY: Float,
    clickAreaWidth: Int,
    onAddIncome: () -> Unit,
) {
    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val x = lerp(fabStartX, buttonLeftX, buttonsShownPercent)
                val y = lerp(
                    fabStartY,
                    sideButtonsY - FAB_BUTTON_SIZE.roundToPx(),
                    buttonsShownPercent
                )

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = x.roundToInt(),
                        y = y.roundToInt()
                    )
                }
            }
            .size(FAB_BUTTON_SIZE)
            .zIndex(200f)
            .testTag("add_income"),
        icon = R.drawable.ic_income,
        backgroundGradient = GradientGreen,
        tint = White,
        onClick = onAddIncome
    )

    Text(
        modifier = Modifier
            .width(FAB_BUTTON_SIZE + 16.dp)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = buttonLeftX.roundToInt() - 8.dp.roundToPx(),
                        y = (sideButtonsY + 12.dp.toPx()).roundToInt()
                    )
                }
            }
            .alpha(buttonsShownPercent)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddIncome()
            }
            .zIndex(200f),
        text = stringResource(R.string.add_income_uppercase),
        style = UI.typo.c.style(
            color = UI.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    )

    // Click area
    Spacer(
        modifier = Modifier
            .size(
                width = clickAreaWidth.toDensityDp(),
                height = TRN_BUTTON_CLICK_AREA_HEIGHT
            )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = 0,
                        y = sideButtonsY.roundToInt() - FAB_BUTTON_SIZE.roundToPx() - 16.dp.roundToPx()
                    )
                }
            }
            .zIndex(199f)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddIncome()
            }
    )
}

@Composable
private fun AddExpenseButton(
    buttonsShownPercent: Float,
    fabStartX: Float,
    fabStartY: Float,
    buttonCenterY: Float,
    clickAreaWidth: Int,
    onAddExpense: () -> Unit,
) {
    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val y =
                    lerp(
                        fabStartY,
                        buttonCenterY - FAB_BUTTON_SIZE.roundToPx(),
                        buttonsShownPercent
                    )

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = fabStartX.roundToInt(),
                        y = y.roundToInt()
                    )
                }
            }
            .size(FAB_BUTTON_SIZE)
            .zIndex(200f)
            .testTag("add_expense"),
        icon = R.drawable.ic_expense,
        backgroundGradient = gradientExpenses(),
        horizontalGradient = false,
        tint = White,
        onClick = onAddExpense
    )

    Text(
        modifier = Modifier
            .width(FAB_BUTTON_SIZE + 16.dp)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = fabStartX.roundToInt() - 8.dp.roundToPx(),
                        y = (buttonCenterY + 12.dp.toPx()).roundToInt()
                    )
                }
            }
            .alpha(buttonsShownPercent)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddExpense()
            }
            .zIndex(200f),
        text = stringResource(R.string.add_expense_uppercase),
        style = UI.typo.c.style(
            color = UI.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    )

    // Click area
    Spacer(
        modifier = Modifier
            .size(
                width = clickAreaWidth.toDensityDp(),
                height = TRN_BUTTON_CLICK_AREA_HEIGHT
            )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = clickAreaWidth,
                        y = buttonCenterY.roundToInt() - FAB_BUTTON_SIZE.roundToPx() - 16.dp.roundToPx()
                    )
                }
            }
            .zIndex(199f)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddExpense()
            }
    )
}

@Composable
private fun AddTransferButton(
    buttonsShownPercent: Float,
    fabStartX: Float,
    fabStartY: Float,
    buttonRightX: Float,
    sideButtonsY: Float,
    clickAreaWidth: Int,
    onAddTransfer: () -> Unit,
) {
    IvyCircleButton(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val x = lerp(fabStartX, buttonRightX, buttonsShownPercent)
                val y = lerp(
                    fabStartY,
                    sideButtonsY - FAB_BUTTON_SIZE.roundToPx(),
                    buttonsShownPercent
                )

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = x.roundToInt(),
                        y = y.roundToInt()
                    )
                }
            }
            .size(FAB_BUTTON_SIZE)
            .zIndex(200f)
            .testTag("add_transfer"),
        icon = R.drawable.ic_transfer,
        backgroundGradient = GradientIvy,
        tint = White,
        onClick = onAddTransfer
    )

    Text(
        modifier = Modifier
            .width(FAB_BUTTON_SIZE + 16.dp)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = buttonRightX.roundToInt() - 8.dp.roundToPx(),
                        y = (sideButtonsY + 12.dp.toPx()).roundToInt()
                    )
                }
            }
            .alpha(buttonsShownPercent)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddTransfer()
            }
            .zIndex(200f),
        text = stringResource(R.string.account_transfer),
        style = UI.typo.c.style(
            color = UI.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    )

    // Click area
    Spacer(
        modifier = Modifier
            .size(
                width = clickAreaWidth.toDensityDp(),
                height = TRN_BUTTON_CLICK_AREA_HEIGHT
            )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = 2 * clickAreaWidth,
                        y = sideButtonsY.roundToInt() - FAB_BUTTON_SIZE.roundToPx() - 16.dp.roundToPx()
                    )
                }
            }
            .zIndex(199f)
            .clickableNoIndication(rememberInteractionSource()) {
                onAddTransfer()
            }
    )
}

@Composable
private fun RowScope.Tab(
    @DrawableRes icon: Int,
    name: String,
    selected: Boolean,
    selectedColor: Color,
    capsuleArrangement: Arrangement.Horizontal,
    capsulePadding: PaddingValues,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) White else UI.colors.pureInverse,
        animationSpec = springBounceFast()
    )

    val capsuleColor by animateColorAsState(
        targetValue = if (selected) selectedColor else Color.Transparent,
        animationSpec = springBounceFast()
    )

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = springBounceFast()
    )

    val tabShape = RoundedCornerShape(percent = 50)

    Row(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = TAB_MIN_TOUCH_TARGET)
            .padding(capsulePadding)
            .clip(tabShape)
            .selectable(
                selected = selected,
                interactionSource = rememberInteractionSource(),
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .testTag(name.lowercase()),
        horizontalArrangement = capsuleArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(tabShape)
                .background(
                    color = capsuleColor,
                    shape = tabShape
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IvyIcon(
                modifier = Modifier.size(24.dp).scale(iconScale),
                icon = icon,
                tint = contentColor,
                contentDescription = name
            )

            if (selected) {
                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = name,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = UI.typo.c.style(
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                )
            }
        }
    }
}
