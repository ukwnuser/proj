package com.callback.app.sms

/**
 * Parses inbound SMS replies for lead intent (stub).
 * Returns true when the message looks like a positive callback request.
 */
object ReplyParser {
    private val positivePatterns = listOf(
        Regex("(?i)\\bcall\\s*me\\b"),
        Regex("(?i)\\byes\\b"),
        Regex("(?i)\\bok\\b"),
        Regex("(?i)\\bcallback\\b"),
    )

    fun isPositiveReply(body: String?): Boolean {
        val text = body?.trim().orEmpty()
        if (text.isEmpty()) return false
        return positivePatterns.any { it.containsMatchIn(text) }
    }
}
