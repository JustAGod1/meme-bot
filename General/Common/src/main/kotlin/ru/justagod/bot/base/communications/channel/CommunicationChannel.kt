package ru.justagod.bot.base.communications.channel

import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.MessageToken
import ru.justagod.bot.base.communications.QuestionFuture
import ru.justagod.bot.base.communications.question.Question

interface CommunicationChannel {

    val userId: Long

    fun sendMessage(msg: Message): MessageToken
    fun sendMessage(msg: String): MessageToken = sendMessage(Message(msg))

    fun ask(question: Question): QuestionFuture
    fun dropQuestion(question: Question)
}