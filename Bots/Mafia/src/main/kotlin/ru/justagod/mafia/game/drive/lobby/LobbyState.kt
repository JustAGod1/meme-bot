package ru.justagod.mafia.game.drive.lobby

import ru.justagod.bot.base.communications.channel.CommunicationChannel

interface LobbyState {

    fun printStatus(communicator: CommunicationChannel)

}