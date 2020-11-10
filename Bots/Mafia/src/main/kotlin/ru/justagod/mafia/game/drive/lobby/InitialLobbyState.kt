package ru.justagod.mafia.game.drive.lobby

import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.mafia.telegram.command.CommandStatus

class InitialLobbyState : LobbyState {
    override fun printStatus(communicator: CommunicationChannel) {
        communicator.sendMessage(
            Message(MESSAGE)
                .inlineKeyboard()
                .row()
                .column("Запустить") { CommandStatus.execute("", communicator) }
                .endRow()
                .endInlineKeyboard()
        )
    }

    companion object {
        const val MESSAGE = "На данный момент игра не запущенна. Вы можете начать новую используя команду /start"
    }
}