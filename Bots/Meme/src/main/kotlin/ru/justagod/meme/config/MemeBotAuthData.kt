package ru.justagod.meme.config

class MemeBotAuthData(
    val username: String,
    val token: String
) {
    companion object {
        fun read() = MemeBotAuthData(
            System.getenv("config.auth.username"),
            System.getenv("config.auth.token")
        )
    }
}