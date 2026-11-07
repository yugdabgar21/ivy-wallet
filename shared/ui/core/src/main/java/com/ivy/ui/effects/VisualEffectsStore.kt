package com.ivy.ui.effects

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.ivy.base.legacy.SharedPrefs
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

// -------------------------------------------------------------------------------------------------
// Visual effects model
// -------------------------------------------------------------------------------------------------

enum class VisualEffectComponent {
    BOTTOM_NAV,
    DASHBOARD_CARDS,
    TRANSACTION_CARDS,
    FAB,
    BOTTOM_SHEETS,
    DIALOGS,
    SETTINGS_PANELS,
}

@Immutable
data class VisualEffectsState(
    val enabled: Boolean = true,
    val bottomNav: Boolean = true,
    val dashboardCards: Boolean = true,
    val transactionCards: Boolean = true,
    val fab: Boolean = true,
    val bottomSheets: Boolean = true,
    val dialogs: Boolean = true,
    val settingsPanels: Boolean = true,
    val scrollAnimations: Boolean = true,
    val intensity: Int = VisualEffectsIntensity.DEFAULT,
) {
    fun isComponentEnabled(component: VisualEffectComponent): Boolean {
        if (!enabled) return false
        return when (component) {
            VisualEffectComponent.BOTTOM_NAV -> bottomNav
            VisualEffectComponent.DASHBOARD_CARDS -> dashboardCards
            VisualEffectComponent.TRANSACTION_CARDS -> transactionCards
            VisualEffectComponent.FAB -> fab
            VisualEffectComponent.BOTTOM_SHEETS -> bottomSheets
            VisualEffectComponent.DIALOGS -> dialogs
            VisualEffectComponent.SETTINGS_PANELS -> settingsPanels
        }
    }

    companion object {
        val Default = VisualEffectsState()
    }
}

/**
 * Maps the 0..100 user setting to the visual strength of the translucent glass styling.
 * It is intentionally a pure function so it can be unit tested without Android.
 */
object VisualEffectsIntensity {
    const val MIN = 0
    const val MAX = 100
    const val DEFAULT = 60

    fun clamp(value: Int): Int = value.coerceIn(MIN, MAX)

    fun fraction(value: Int): Float = clamp(value) / 100f

    fun overlayAlpha(value: Int): Float = 0.10f * fraction(value)

    fun borderAlpha(value: Int): Float = 0.30f * fraction(value)

    fun shadowElevationDp(value: Int): Int = (14f * fraction(value)).roundToInt()

    /**
     * How far the home header shifts per scrolled pixel when the optional scroll animation is on.
     */
    fun scrollTranslationMultiplier(value: Int): Float = 0.10f + 0.20f * fraction(value)
}

// -------------------------------------------------------------------------------------------------
// Observable store
// -------------------------------------------------------------------------------------------------

/**
 * Single source of truth for the "Material Liquid Glass" styling settings.
 *
 * Every value is backed by a Compose [mutableStateOf], so any composable that reads it updates
 * immediately when the user changes a switch. All values are persisted to [SharedPrefs].
 */
@Stable
@Singleton
class VisualEffectsStore @Inject constructor(
    private val sharedPrefs: SharedPrefs,
) {
    var enabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_ENABLED, true)
    )
        private set

    var bottomNavEnabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_BOTTOM_NAV, true)
    )
        private set

    var dashboardCardsEnabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_DASHBOARD_CARDS, true)
    )
        private set

    var transactionCardsEnabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_TRANSACTION_CARDS, true)
    )
        private set

    var fabEnabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_FAB, true)
    )
        private set

    var bottomSheetsEnabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_BOTTOM_SHEETS, true)
    )
        private set

    var dialogsEnabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_DIALOGS, true)
    )
        private set

    var settingsPanelsEnabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_SETTINGS_PANELS, true)
    )
        private set

    var scrollAnimationsEnabled by mutableStateOf(
        sharedPrefs.getBoolean(SharedPrefs.VISUAL_EFFECTS_SCROLL_ANIMATIONS, true)
    )
        private set

    var intensity by mutableIntStateOf(
        VisualEffectsIntensity.clamp(
            sharedPrefs.getInt(
                SharedPrefs.VISUAL_EFFECTS_INTENSITY,
                VisualEffectsIntensity.DEFAULT
            )
        )
    )
        private set

    fun componentEnabled(component: VisualEffectComponent): Boolean {
        if (!enabled) return false
        return when (component) {
            VisualEffectComponent.BOTTOM_NAV -> bottomNavEnabled
            VisualEffectComponent.DASHBOARD_CARDS -> dashboardCardsEnabled
            VisualEffectComponent.TRANSACTION_CARDS -> transactionCardsEnabled
            VisualEffectComponent.FAB -> fabEnabled
            VisualEffectComponent.BOTTOM_SHEETS -> bottomSheetsEnabled
            VisualEffectComponent.DIALOGS -> dialogsEnabled
            VisualEffectComponent.SETTINGS_PANELS -> settingsPanelsEnabled
        }
    }

    fun snapshot(): VisualEffectsState = VisualEffectsState(
        enabled = enabled,
        bottomNav = bottomNavEnabled,
        dashboardCards = dashboardCardsEnabled,
        transactionCards = transactionCardsEnabled,
        fab = fabEnabled,
        bottomSheets = bottomSheetsEnabled,
        dialogs = dialogsEnabled,
        settingsPanels = settingsPanelsEnabled,
        scrollAnimations = scrollAnimationsEnabled,
        intensity = intensity,
    )

    fun setMasterEnabled(value: Boolean) {
        enabled = value
        sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_ENABLED, value)
    }

    fun setComponentEnabled(component: VisualEffectComponent, value: Boolean) {
        when (component) {
            VisualEffectComponent.BOTTOM_NAV -> {
                bottomNavEnabled = value
                sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_BOTTOM_NAV, value)
            }

            VisualEffectComponent.DASHBOARD_CARDS -> {
                dashboardCardsEnabled = value
                sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_DASHBOARD_CARDS, value)
            }

            VisualEffectComponent.TRANSACTION_CARDS -> {
                transactionCardsEnabled = value
                sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_TRANSACTION_CARDS, value)
            }

            VisualEffectComponent.FAB -> {
                fabEnabled = value
                sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_FAB, value)
            }

            VisualEffectComponent.BOTTOM_SHEETS -> {
                bottomSheetsEnabled = value
                sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_BOTTOM_SHEETS, value)
            }

            VisualEffectComponent.DIALOGS -> {
                dialogsEnabled = value
                sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_DIALOGS, value)
            }

            VisualEffectComponent.SETTINGS_PANELS -> {
                settingsPanelsEnabled = value
                sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_SETTINGS_PANELS, value)
            }
        }
    }

    fun setScrollAnimations(value: Boolean) {
        scrollAnimationsEnabled = value
        sharedPrefs.putBoolean(SharedPrefs.VISUAL_EFFECTS_SCROLL_ANIMATIONS, value)
    }

    fun setIntensityLevel(value: Int) {
        val clamped = VisualEffectsIntensity.clamp(value)
        intensity = clamped
        sharedPrefs.putInt(SharedPrefs.VISUAL_EFFECTS_INTENSITY, clamped)
    }
}

/**
 * Provided once by the root activity. It is intentionally nullable so previews and screenshot
 * tests keep the original look unless a test explicitly provides the store.
 */
val LocalVisualEffectsStore = staticCompositionLocalOf<VisualEffectsStore?> { null }
