package ru.justagod.bot.start

import org.telegram.telegrambots.meta.TelegramBotsApi

interface BotMain<Config> {

    fun run(api: TelegramBotsApi, config: Config)

    fun name(): String

}