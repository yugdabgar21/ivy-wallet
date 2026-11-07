package com.ivy.wallet

import android.os.Process
import android.os.SystemClock
import timber.log.Timber

/**
 * Debug-only cold start milestones.
 *
 * Every line is expressed as milliseconds since the process was started, which is the metric
 * Android itself uses for "cold start". Run the app on a real phone with `adb logcat` filtered by
 * the `STARTUP` tag to see where the time actually goes before changing any startup code.
 */
internal object StartupTrace {

    private val processStart: Long = Process.getStartElapsedRealtime()

    fun mark(label: String) {
        if (!BuildConfig.DEBUG) return
        val sinceProcessStart = SystemClock.elapsedRealtime() - processStart
        Timber.d("STARTUP %s: %d ms since process start", label, sinceProcessStart)
    }
}
