package ru.justagod.mafia.telegram.command

import org.telegram.telegrambots.meta.api.objects.Message
import ru.justagod.bot.base.command.Command
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.mafia.game.drive.lobby.Lobby

object CommandStatus : Command("status", "Показать текущий статус") {
    override fun execute(msg: String, channel: CommunicationChannel) {
        Lobby.state.printStatus(channel)
    }
}