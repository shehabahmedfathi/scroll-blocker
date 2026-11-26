package com.scrollpause.app

data class Quote(
    val text: String,
    val author: String
)

object MotivationalQuotes {
    private val quotes = listOf(
        Quote(
            "How we spend our days is, of course, how we spend our lives.",
            "Annie Dillard"
        ),
        Quote(
            "You do not rise to the level of your goals. You fall to the level of your systems.",
            "James Clear"
        ),
        Quote(
            "Where your attention goes, your time and energy flow.",
            "Tony Robbins"
        ),
        Quote(
            "Almost everything will work again if you unplug it for a few minutes, including you.",
            "Anne Lamott"
        ),
        Quote(
            "Discipline is choosing between what you want now and what you want most.",
            "Abraham Lincoln"
        ),
        Quote(
            "Your focus determines your reality. Protect it deliberately.",
            "George Lucas"
        ),
        Quote(
            "The key is not to prioritize what's on your schedule, but to schedule your priorities.",
            "Stephen Covey"
        ),
        Quote(
            "Small intentional pauses turn autopilot into purpose.",
            "ScrollPause"
        )
    )

    fun random(): Quote = quotes.random()
}