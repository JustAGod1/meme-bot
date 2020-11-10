package ru.justagod.bot.base.communications.question

import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.QuestionFuture
import ru.justagod.bot.base.communications.channel.CommunicationChannel

class TextQuestion(
    private val prompt: String,
    private val communicator: CommunicationChannel
) : Question {

    override lateinit var future: QuestionFuture

    override fun ask() {
        communicator.sendMessage(Message(prompt))
    }

    override fun receiveMessage(msg: String) {
        future.set(msg)
    }

    override fun drop() {
        future.cancel(false)
    }
}