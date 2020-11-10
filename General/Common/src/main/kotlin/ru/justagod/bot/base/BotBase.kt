package ru.justagod.bot.base

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.justagod.bot.base.command.Command
import ru.justagod.bot.base.communications.Communicator
import ru.justagod.bot.base.communications.MessageToken
import ru.justagod.bot.base.communications.QuestionFuture
import ru.justagod.bot.base.communications.channel.StubCommunicationChannel
import ru.justagod.bot.base.communications.question.Question
import ru.justagod.bot.base.model.InlineButtonData
import java.lang.Exception
import java.util.*
import kotlin.collections.LinkedHashMap

abstract class BotBase : TelegramLongPollingBot(), Communicator {
    protected val commands = LinkedHashMap<String, Command>()

    private val questions = hashMapOf<Pair</*chatId*/Long, /*userId*/Long>, Question>()
    private val questionsInv = hashMapOf<Question, Pair</*chatId*/Long, /*userId*/Long>>()

    private val buttons = hashMapOf<UUID, InlineButtonData>()

    protected fun registerCommand(command: Command) {
        commands[command.name] = command
    }


    override fun onUpdateReceived(update: Update) {
        if (update.hasCallbackQuery()) {
            processCallback(update.callbackQuery)
        }
        if (update.hasMessage()) {
            val message = update.message
            if (message.hasText()) {
                val text = message.text
                val question = questions[message.chatId to message.from?.id?.toLong()]
                if (question != null) {
                    question.receiveMessage(text)
                } else {
                    handleCommand(message)
                }
            }
        }
    }

    protected fun processCallback(callback: CallbackQuery) {
        val answer = AnswerCallbackQuery()
        answer.callbackQueryId = callback.id
        val data = callback.data
        if (data != null) {
            var uuid: UUID? = null
            try {
                uuid = UUID.fromString(data)
            } catch (e: Exception) {
                answer.text = "error"
            }
            if (uuid != null) {
                val button = buttons[uuid]
                if (button != null) {
                    answer.text = button.callback.onClick(button.token, callback.from.id.toLong())
                } else {
                    answer.text = "no longer available"
                }
            }
        }
        execute(answer)
    }

    protected fun handleCommand(msg: Message): Boolean {
        if (msg.from?.bot != false) return false
        val commandName = msg.text.drop(1).split("\\s+".toRegex())[0]
        val cmd = commands[commandName]
        if (cmd != null) {
            cmd.execute(msg.text, StubCommunicationChannel(this, msg.chatId, msg.from.id.toLong()))
            return true
        }
        return false

    }

    fun sendSimpleMsg(userId: Long, msg: String) {
        val msgCommand = SendMessage()
        msgCommand.text = msg
        msgCommand.setChatId(userId)

        execute(msgCommand)
    }

    override fun dropQuestion(question: Question) {
        val p = questionsInv.remove(question) ?: return
        questions.remove(p)?.drop()
    }


    override fun sendMessage(chatId: Long, msg: ru.justagod.bot.base.communications.Message): MessageToken {
        val order = SendMessage(chatId, msg.text)

        val token = TelegramMessageToken()
        if (msg.replyKeyboard != null) {
            val data = msg.replyKeyboard!!
            val keyboard = ReplyKeyboardMarkup(
                data.map { row ->
                    KeyboardRow().also { it.addAll(row) }
                }
            )
            order.replyMarkup = keyboard
        }
        if (msg.inlineKeyboard != null) {
            val data = msg.inlineKeyboard!!
            val keyboard = InlineKeyboardMarkup()
            val layout = arrayListOf<MutableList<InlineKeyboardButton>>()
            for (rowData in data) {
                val row = arrayListOf<InlineKeyboardButton>()
                for (column in rowData) {
                    val btn = InlineKeyboardButton(column.text)
                    val uuid = UUID.randomUUID()
                    token.uuids += uuid
                    btn.callbackData = uuid.toString()
                    buttons[uuid] = InlineButtonData(column.callback, token)
                    row += btn
                }
                layout += row
            }
            keyboard.keyboard = layout
            order.replyMarkup = keyboard
        }

        val m = execute(order)

        token.chatId = m.chatId
        token.messageId = m.messageId

        return token
    }

    override fun ask(chatId: Long, userId: Long, question: Question): QuestionFuture {
        val future = QuestionFuture(question, this)
        val oldOne = questions.put(chatId to userId, question)
        questionsInv[question] = chatId to userId
        oldOne?.drop()
        question.future = future
        question.ask()

        return future
    }


    private inner class TelegramMessageToken : MessageToken {
        val uuids = arrayListOf<UUID>()
        var chatId: Long = 0
        var messageId: Int = 0

        override fun deanimate() {
            for (uuid in uuids) {
                buttons -= uuid
            }
            execute(
                EditMessageReplyMarkup()
                    .setChatId(chatId)
                    .setMessageId(messageId)
            )
        }
    }
}