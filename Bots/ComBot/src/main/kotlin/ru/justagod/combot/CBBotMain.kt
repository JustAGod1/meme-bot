package ru.justagod.combot

import org.apache.logging.log4j.LogManager
import org.telegram.telegrambots.meta.TelegramBotsApi
import ru.justagod.bot.start.BotMain
import ru.justagod.combot.config.CBConfig
import ru.justagod.combot.telegram.CBBot
import java.text.SimpleDateFormat

object CBBotMain : BotMain<CBConfig> {
    val format = SimpleDateFormat("yyyy-MM-dd")

    lateinit var config: CBConfig
        private set

    val logger = LogManager.getLogger("combot")

    override fun run(api: TelegramBotsApi, config: CBConfig) {
        this.config = config

        api.registerBot(CBBot())
    }

    override fun name(): String = "cb"
}