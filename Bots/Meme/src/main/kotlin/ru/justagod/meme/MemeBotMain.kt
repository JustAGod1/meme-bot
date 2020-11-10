package ru.justagod.meme

import com.beust.klaxon.Klaxon
import org.apache.logging.log4j.LogManager
import org.telegram.telegrambots.meta.TelegramBotsApi
import ru.justagod.bot.start.BotMain
import ru.justagod.meme.config.MemeBotConfig
import ru.justagod.meme.data.Defender
import ru.justagod.meme.data.MemesStorage
import ru.justagod.meme.telegram.MemeBot
import java.io.File
import java.lang.Exception

object MemeBotMain : BotMain<MemeBotConfig> {

    val logger = LogManager.getLogger("meme")!!

    lateinit var config: MemeBotConfig
        private set
    override fun run(api: TelegramBotsApi, config: MemeBotConfig) {
        this.config = config

        MemesStorage.syncData()
        Defender.syncData()
        api.registerBot(
            MemeBot
        )
    }

    override fun name(): String = "meme"


}