package ru.justagod.meme.telegram

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import ru.justagod.bot.base.BotBase
import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.meme.MemeBotMain
import ru.justagod.meme.telegram.command.*
import ru.justagod.meme.telegram.dialog.Dialog
import ru.justagod.meme.telegram.query.QueriesManager
import java.lang.StringBuilder


object MemeBot : BotBase() {


    private val helpMessage: String by lazy { makeHelpMessage() }
    private val dialogs = hashMapOf<Long, Dialog>()

    init {
        registerCommand(CommandHelp)
        registerCommand(CommandStart)
        registerCommand(CommandAdd)
        registerCommand(CommandList)
        registerCommand(CommandDelete)
    }


    override fun onUpdateReceived(update: Update) {
        if (update.hasCallbackQuery()) processCallback(update.callbackQuery)
        if (update.hasInlineQuery()) {
            val inlineQuery = update.inlineQuery
            val answer = AnswerInlineQuery()
            answer.results = QueriesManager.makeInlineQueryResults(inlineQuery.query, inlineQuery.from.id.toLong())
            answer.inlineQueryId = inlineQuery.id
            answer.cacheTime = 10

            execute(answer)
        } else if (update.hasMessage()) {
            val msg = update.message
            if (!msg.isUserMessage) return
            val dialog = dialogs[msg.chatId]
            if (dialog != null) {
                dialog.onUpdateReceived(msg, this)
            } else super.onUpdateReceived(update)
        }
    }

    fun startDialog(userId: Long, dialog: Dialog) {
        dialogs[userId] = dialog
        dialog.start(this)
    }

    fun dropDialog(userId: Long) {
        dialogs -= userId
    }

    override fun getBotUsername(): String = MemeBotMain.config.auth.username

    override fun getBotToken(): String = MemeBotMain.config.auth.token

    fun sendHelpMessage(channel: CommunicationChannel) {
        channel.sendMessage(Message(helpMessage))
    }

    private fun makeHelpMessage() : String {
        operator fun StringBuilder.plusAssign(rhs: Any) {
            this.append(rhs)
        }
        val sb = StringBuilder()
        sb += """
            Я бот, цель которого помогоать в поисках мемов по тегам для быстрых реакций в чатах.
            
            Во мне хранится огромная база мемов, каждому из которых присвоен свой уникальный тег.
            
            Стандартное мое использование - это через инлайн запросы. Просто наберите в строке ввода `@jameme_bot` в любом чате и найдите нужный мем по тегу
            
            Список доступных комманд:
            
        """.trimIndent()

        for (command in commands.values) {
            if (!command.showInHelp()) continue
            sb += "/"
            sb += command.name
            sb += " - "
            sb += command.description
            sb += "\n"
        }

        return sb.toString()
    }

}