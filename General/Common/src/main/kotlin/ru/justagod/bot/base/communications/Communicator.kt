package ru.justagod.bot.base.communications

import ru.justagod.bot.base.communications.question.Question

interface Communicator {

    fun sendMessage(chatId: Long, msg: Message): MessageToken

    fun ask(
        chatId: Long,
        userId: Long,
        question: Question
    ): QuestionFuture

    fun dropQuestion(question: Question)

}