package ru.justagod.memeBot

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.Marker
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import ru.justagod.bot.start.BotMain
import ru.justagod.combot.CBBotMain
import ru.justagod.mafia.MafiaBotMain
import ru.justagod.meme.MemeBotMain
import java.io.File
import java.io.PrintStream
import java.lang.Exception
import java.lang.reflect.ParameterizedType
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.system.exitProcess

object Main {


    private val bots = listOf<BotMain<*>>(
        MafiaBotMain,
        MemeBotMain
    )

    private val logger = LogManager.getLogger("general")

    @JvmStatic
    fun main(args: Array<String>) {
        logger.let { logger ->
            System.setOut(TracingPrintStream(System.out, logger, Level.INFO))
            System.setErr(TracingPrintStream(System.err, logger, Level.ERROR))
        }


        ApiContextInitializer.init()
        val api = TelegramBotsApi()

        val klaxon = Klaxon()

        val configFile = File("config.json")

        if (!configFile.exists()) {
            logger.error("config.json does not exist")
            return
        }

        val config = try {
            klaxon.parseJsonObject(configFile.reader())
        } catch (e: Exception) {
            logger.error("Exception while reading config", e)
            return
        }

        val enabled = readEnabledBots(config)
        for (bot in bots) {
            if (enabled?.contains(bot.name()) == false) continue
            if (bot.name() !in config) {
                logger.error("Cannot find config for ${bot.name()}")
                exitProcess(1)
            }
            val part = config[bot.name()]

            if (part !is JsonObject) {
                logger.error("Expected JsonObject for key ${bot.name()} but got ${part?.javaClass?.simpleName}")
                exitProcess(1)
            }
            val configType = findConfigType(bot)

            val configPart = try {
                klaxon.fromJsonObject(part, configType, configType.kotlin)
            } catch (e: Exception) {
                logger.error("Exception while trying to parse ${bot.name()} config part into ${configType.name}")
                exitProcess(1)
            }

            thread(name = bot.name() + " thread") {
                (bot as BotMain<Any>).run(api, configPart)
            }
        }

    }

    private fun readEnabledBots(config: JsonObject): Set<String>? {
        val field = config["enabled"] ?: return null
        if (field !is JsonArray<*>) error("enabled is not array")
        return field.map { if (it !is String) error("$it is not string") else it }.toSet()
    }

    private fun findConfigType(main: BotMain<*>): Class<*> {
        val clazz = main.javaClass
        for (type in clazz.genericInterfaces) {
            if (type is ParameterizedType) {
                if (type.rawType.typeName == BotMain::class.java.typeName) {
                    return Class.forName(type.actualTypeArguments[0].typeName)
                }
            }
        }

        error("")
    }


    class TracingPrintStream(
        original: PrintStream,
        private val logger: Logger,
        private val level: Level = Level.INFO,
        private val prefix: String? = null
    ) : PrintStream(original) {

        private val prefixFormatted = if (prefix != null) "$prefix " else ""

        override fun println(x: Any?) = lo(x ?: "")
        override fun println(x: Boolean) = lo(x)
        override fun println(x: Char) = lo(x)
        override fun println(x: CharArray) = lo(x)
        override fun println(x: Double) = lo(x)
        override fun println(x: Float) = lo(x)
        override fun println(x: Int) = lo(x)
        override fun println(x: Long) = lo(x)
        override fun println(x: String?) = lm(x ?: "")
        override fun println() = lm("")

        private fun lo(message: Any) {
            logger.log(level, prefixFormatted + message)
        }

        private fun lm(message: String) {
            logger.log(level, prefixFormatted + message)
        }
    }
}