package ru.justagod.bot.base.communications.channel

import ru.justagod.bot.base.communications.Communicator
import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.MessageToken
import ru.justagod.bot.base.communications.QuestionFuture
import ru.justagod.bot.base.communications.question.Question

class StubCommunicationChannel(
    private val communicator: Communicator,
    private val chatId: Long,
    override val userId: Long
) : CommunicationChannel{
    override fun sendMessage(msg: Message): MessageToken {
        return communicator.sendMessage(chatId, msg)
    }

    override fun ask(question: Question): QuestionFuture {
        return communicator.ask(chatId, userId, question)
    }

    override fun dropQuestion(question: Question) {
        communicator.dropQuestion(question)
    }
}