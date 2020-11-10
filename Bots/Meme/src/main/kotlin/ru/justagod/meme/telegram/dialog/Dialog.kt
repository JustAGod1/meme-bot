package ru.justagod.meme.telegram.dialog

import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import ru.justagod.meme.telegram.MemeBot

abstract class Dialog(protected val userId: Long) {

    abstract fun onUpdateReceived(update: Message, bot: MemeBot)

    abstract fun start(bot: MemeBot)


    fun sendSimpleMsg(msg: String, bot: MemeBot) {
        bot.sendSimpleMsg(userId, msg)
    }



}