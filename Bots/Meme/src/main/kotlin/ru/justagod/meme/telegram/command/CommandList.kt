package ru.justagod.meme.telegram.command

import ru.justagod.bot.base.command.Command
import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.meme.data.MemesStorage
import ru.justagod.meme.telegram.MemeBot
import java.lang.StringBuilder

object CommandList : Command("list", "Перечисляет доступные вам мемы") {
    override fun execute(msg: String, channel: CommunicationChannel) {
        val privateMemes = MemesStorage.getPrivateMemes(channel.userId)
        val globalMemes = MemesStorage.getGlobalMemes()

        val sb = StringBuilder()

        if (privateMemes.isNotEmpty()) {
            sb.append("Ваши личные мемы:\n")
            var i = 1
            for (privateMeme in privateMemes) {
                sb.append("  ").append(i).append(". ")
                sb.append(privateMeme.tag).append(" - ").append(privateMeme.desc)
                sb.append("\n")
                i++
            }
        }
        sb.append("\n")
        if (globalMemes.isNotEmpty()) {
            sb.append("Общие мемы:\n")
            var i = 1
            for (globalMeme in globalMemes) {
                sb.append("  ").append(i).append(". ")
                sb.append(globalMeme.tag).append(" - ").append(globalMeme.desc)
                sb.append("\n")
                i++
            }
        }

        channel.sendMessage(Message(sb.toString()))
    }
}