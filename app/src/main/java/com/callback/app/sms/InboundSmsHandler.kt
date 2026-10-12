package com.callback.app.sms

/** Pure logic for persisting inbound SMS on a lead card. */
object InboundSmsHandler {
    fun replyTextToStore(body: String?): String? = body?.trim()?.takeIf { it.isNotEmpty() }
}
