package ru.justagod.bot.base.communications.question

import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.MessageToken
import ru.justagod.bot.base.communications.QuestionFuture
import ru.justagod.bot.base.communications.channel.CommunicationChannel

abstract class QuestionBase(val channel: CommunicationChannel) : Question{
    override lateinit var future: QuestionFuture

    private val tokens = arrayListOf<MessageToken>()

    override fun drop() {
        future.cancel(true)
        tokens.forEach(MessageToken::deanimate)
    }

    protected fun registerToken(token: MessageToken) {
        tokens += token
    }


    protected fun cancel(goodbyeMessage: String?): MessageToken? = cancel(goodbyeMessage?.let { Message(it) })

    protected fun cancel(goodbyeMessage: Message?): MessageToken? {
        val token = goodbyeMessage?.let { channel.sendMessage(goodbyeMessage) }
        future.cancel(true)
        channel.dropQuestion(this)

        return token
    }

    protected fun complete(value: String) {
        future.set(value)
        channel.dropQuestion(this)
    }

}