package ru.justagod.mafia.game.config.base

import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.QuestionFuture
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.bot.base.communications.question.Question
import ru.justagod.bot.base.communications.question.QuestionBase
import ru.justagod.mafia.future.VoidFuture
import ru.justagod.mafia.future.addSuccessCallback
import ru.justagod.mafia.game.config.ConfigNode
import ru.justagod.mafia.game.config.ConfigurationContext

class StringConfigEntry<Data: Any>(
    private val id: String,
    private val name: String,
    private val fullDesc: String,
    default: String?
) : ConfigEntryBase<Data>() {
    var value: String? = default
        private set

    override fun configure(channel: CommunicationChannel, data: Data, context: ConfigurationContext<Data>): ListenableFuture<*> {
        val future = channel.ask(StringQuestion(channel))
        future.addSuccessCallback { it -> value = it }
        return future
    }

    override fun name(): String = name

    override fun printValue(): String = value ?: "NaN"

    override fun fullDesc(): String = fullDesc

    override fun id(): String = id

    private inner class StringQuestion(channel: CommunicationChannel) : QuestionBase(channel) {

        override fun ask() {
            val token = channel.sendMessage(
                Message("Отправьте строку")
                    .inlineKeyboard().addCancelButton(channel.userId, future).endInlineKeyboard()
            )
            registerToken(token)
        }

        override fun receiveMessage(msg: String) {
            future.set(msg)
        }

    }
}