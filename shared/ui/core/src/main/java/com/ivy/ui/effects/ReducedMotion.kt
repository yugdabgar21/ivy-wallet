package com.ivy.ui.effects

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * True when the OS allows animations.
 *
 * Android exposes "Remove animations" as [Settings.Global.ANIMATOR_DURATION_SCALE] == 0.
 * Optional visual effects must stay off in that case so the app respects the accessibility
 * preference of the user.
 */
@Composable
fun rememberSystemAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) != 0f
        }.getOrDefault(true)
    }
}
