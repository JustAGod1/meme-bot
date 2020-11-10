package ru.justagod.meme.config

class MemeBotConfig(
    val owner: Long,
    val auth: MemeBotAuthData,
    val database: MemeBotDataBase
) {
    companion object {
        fun read() = MemeBotConfig(
            System.getenv("config.owner").toLong(),
            MemeBotAuthData.read(),
            MemeBotDataBase.read()
        )
    }
}