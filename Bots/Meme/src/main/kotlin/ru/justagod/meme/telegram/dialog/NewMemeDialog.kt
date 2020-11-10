package ru.justagod.meme.telegram.dialog

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import ru.justagod.meme.MemeBotMain
import ru.justagod.meme.data.Defender
import ru.justagod.meme.data.Meme
import ru.justagod.meme.data.MemesStorage
import ru.justagod.meme.telegram.MemeBot

class NewMemeDialog(userId: Long) : Dialog(userId) {

    private var state = State.WAITING_FOR_IMAGE
    private lateinit var fileId: String
    private lateinit var tag: String
    private lateinit var desc: String

    override fun onUpdateReceived(update: Message, bot: MemeBot) {
        when (state) {
            State.WAITING_FOR_IMAGE -> receiveImage(update, bot)
            State.WAITING_FOR_TAG -> receiveTag(update, bot)
            State.WAITING_FOR_DESC -> receiveDesc(update, bot)
            State.WAITING_FOR_POLICY -> receivePolicy(update, bot)
        }
    }

    private fun receivePolicy(message: Message, bot: MemeBot) {
        if (!message.hasText()) {
            sendSimpleMsg("Сообщение должно содержать текст", bot)
            return
        }

        val text = message.text

        if (text.equals("всем", ignoreCase = true)) {
            val meme = Meme(fileId, tag, desc, null)
            MemeBotMain.logger.info("Adding meme $meme")
            Defender.addMeme(message.chatId, meme)
            sendSimpleMsg("Мем был добавлен в общее пользование.", bot)
            bot.dropDialog(userId)
        } else if (text.equals("мне", ignoreCase = true)) {
            val meme = Meme(fileId, tag, desc, userId)
            MemeBotMain.logger.info("Adding meme $meme")
            Defender.addMeme(message.chatId, meme)
            sendSimpleMsg("Мем был добавлен в вашу личную коллекцию.", bot)
            bot.dropDialog(userId)
        } else {
            sendSimpleMsg("всем/мне", bot)
        }
    }

    private fun receiveDesc(message: Message, bot: MemeBot) {
        if (!message.hasText()){
            sendSimpleMsg("Сообщение должно содержать текст", bot)
            return
        }
        val text = message.text
        if (text.length > 100) {
            sendSimpleMsg("Описание не может быть длиннее 100 символов", bot)
            return
        }
        desc = text

        if (userId == MemeBotMain.config.owner) {
            state = State.WAITING_FOR_POLICY
            sendSimpleMsg("Очень хорошо. Теперь скажите кому будет доступен этом мем.\nВсем/Мне", bot)
        } else {
            val meme = Meme(fileId, tag, desc, userId)
            MemeBotMain.logger.info("Adding meme $meme")
            Defender.addMeme(message.chatId, meme)
            sendSimpleMsg("Мем был добавлен в вашу личную коллекцию.", bot)
            bot.dropDialog(userId)
        }
    }

    private fun receiveTag(message: Message, bot: MemeBot) {
        if (!message.hasText()){
            sendSimpleMsg("Сообщение должно содержать текст", bot)
            return
        }
        val text = message.text
        if (text.length > 48) {
            sendSimpleMsg("Тег не может быть длиннее 48 символов", bot)
            return
        }

        if (MemesStorage.hasTag(text, userId)) {
            sendSimpleMsg("Такой тег уже занят", bot)
            return
        }

        tag = text

        state = State.WAITING_FOR_DESC
        sendSimpleMsg("Великолепно. Теперь введите краткое описание.", bot)
    }

    private fun receiveImage(message: Message, bot: MemeBot) {
        if (!message.hasPhoto()) {
            sendSimpleMsg("Сообщение должно содержать картинку", bot)
            return
        }

        fileId = message.photo[0].fileId

        state = State.WAITING_FOR_TAG
        sendSimpleMsg("Отлично. Теперь введите тэг.", bot)

    }

    override fun start(bot: MemeBot) {
        val msg = SendMessage()
        msg.text = "Отправьте мем"
        msg.setChatId(userId)
        bot.execute(msg)
    }


    private enum class State {
        WAITING_FOR_IMAGE,
        WAITING_FOR_TAG,
        WAITING_FOR_DESC,
        WAITING_FOR_POLICY
    }

}