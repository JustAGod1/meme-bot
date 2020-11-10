package ru.justagod.combot.telegram

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import ru.justagod.combot.CBBotMain
import ru.justagod.combot.request.DataRequester
import ru.justagod.combot.request.RequestsFulfiler

class CBBot : TelegramLongPollingBot() {

    override fun onUpdateReceived(update: Update) {
        if (!update.hasMessage()) return
        val msg = update.message

        if (msg.isCommand) {
            if (msg.text.startsWith("/show")) {
                RequestsFulfiler.fulfilRequest(msg.text.substringAfter(" "), msg.chatId, this)
            }
        }
    }

    fun sendSimpleMsg(userId: Long, msg: String) {
        val msgCommand = SendMessage()
        msgCommand.text = msg
        msgCommand.setChatId(userId)
        msgCommand.enableMarkdown(true)

        execute(msgCommand)
    }

    override fun getBotUsername(): String = CBBotMain.config.username

    override fun getBotToken(): String = CBBotMain.config.token

}