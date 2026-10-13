package com.callback.app.sms

/** At-most-once auto-text: persist attempt before invoking SMS APIs. */
object AutoTextSendPlan {
    enum class Action {
        SKIP,
        RECORD_THEN_SEND,
    }

    fun action(maySend: Boolean): Action =
        if (maySend) Action.RECORD_THEN_SEND else Action.SKIP
}
