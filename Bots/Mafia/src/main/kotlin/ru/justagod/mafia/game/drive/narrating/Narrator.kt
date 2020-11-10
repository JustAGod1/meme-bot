package ru.justagod.mafia.game.drive.narrating

import ru.justagod.bot.base.communications.Communicator
import ru.justagod.bot.base.communications.Message
import ru.justagod.mafia.game.drive.narrating.model.Member
import ru.justagod.mafia.telegram.model.User

interface Narrator {

    fun run(participants: List<User>, requester: Long, bot: Communicator) : Boolean

    fun status()

    fun makeAnnouncement(message: Message)

    fun sendPrivateMessage(message: Message, member: Member)

}