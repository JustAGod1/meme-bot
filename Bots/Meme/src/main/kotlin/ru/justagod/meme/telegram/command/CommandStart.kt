package ru.justagod.meme.telegram.command

import org.telegram.telegrambots.meta.api.objects.Message
import ru.justagod.bot.base.command.Command
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.meme.telegram.MemeBot

object CommandStart : Command("start", ""){
    override fun execute(msg: String, channel: CommunicationChannel) {
        MemeBot.sendHelpMessage(channel)
    }

    override fun showInHelp() = false
}