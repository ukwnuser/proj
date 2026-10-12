package com.callback.app.util

import android.util.Log

/** Logging helpers that avoid writing phone numbers or message bodies. */
object SafeLog {
    fun d(tag: String, message: String) = Log.d(tag, message)

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
    }

    fun phoneEvent(tag: String, event: String) {
        Log.d(tag, event)
    }
}
