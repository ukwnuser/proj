package com.callback.app.detect

import java.util.concurrent.TimeUnit

/** Prevents auto-texting the same number more than once within 24 hours. */
object TextRepeatPolicy {
    val repeatWindowMs: Long = TimeUnit.HOURS.toMillis(24)

    fun mayAutoText(lastTextedAtEpochMs: Long?, nowEpochMs: Long): Boolean {
        if (lastTextedAtEpochMs == null) return true
        return nowEpochMs - lastTextedAtEpochMs >= repeatWindowMs
    }
}
