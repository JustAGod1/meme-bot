package ru.justagod.bot.base.communications.question

import ru.justagod.bot.base.communications.QuestionFuture

interface Question {

    var future: QuestionFuture

    fun ask()

    fun receiveMessage(msg: String)

    fun drop()

}