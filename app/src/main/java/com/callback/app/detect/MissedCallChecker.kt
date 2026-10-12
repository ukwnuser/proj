package com.callback.app.detect

import com.callback.app.data.TextedNumber

/** Repeat guard used before enqueueing auto-text work. */
class MissedCallChecker {
    fun shouldAutoText(textedNumber: TextedNumber?, nowEpochMs: Long): Boolean =
        TextRepeatPolicy.mayAutoText(textedNumber?.lastTextedAtEpochMs, nowEpochMs)
}
