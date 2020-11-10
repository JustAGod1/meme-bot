package ru.justagod.bot.base.command

import org.telegram.telegrambots.meta.api.objects.Message
import ru.justagod.bot.base.BotBase
import ru.justagod.bot.base.communications.channel.CommunicationChannel

abstract class Command(val name: String, val description: String) {

    abstract fun execute(msg: String, channel: CommunicationChannel)

    open fun showInHelp() = true

}