package ru.justagod.mafia

import org.telegram.telegrambots.meta.TelegramBotsApi
import ru.justagod.bot.start.BotMain
import ru.justagod.mafia.telegram.MafiaBot

object MafiaBotMain : BotMain<MafiaBotMain.MafiaConfig>{

    lateinit var config: MafiaConfig
        private set
    class MafiaConfig(val username: String, val token: String)

    override fun run(api: TelegramBotsApi, config: MafiaConfig) {
        this.config = config
        api.registerBot(MafiaBot())
    }

    override fun name() = "mafia"
}