package ru.justagod.meme.telegram.command

import ru.justagod.bot.base.command.Command
import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.MessageToken
import ru.justagod.bot.base.communications.QuestionFuture
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.bot.base.communications.question.Question
import ru.justagod.bot.base.communications.question.QuestionBase
import ru.justagod.meme.data.MemesStorage

object CommandDelete : Command("delete", "Удалить мем"){

    override fun execute(msg: String, channel: CommunicationChannel) {
        channel.ask(TagQuestion(channel)).onDone { deleteMeme(channel.userId, it) }
    }

    private fun deleteMeme(userId: Long, tag: String) {
        MemesStorage.deleteMeme(userId, tag)
    }

    private class TagQuestion(channel: CommunicationChannel) : QuestionBase(channel) {
        private val myMemes = MemesStorage.getPrivateMemes(channel.userId).map { it.tag }.toSet()
        override fun ask() {
            registerToken(channel.sendMessage(
                Message("Введите тег мема, который хотите удалить")
                    .inlineKeyboard()
                    .row().column("Отмена") { cancel("Удаление отменено") }.endRow().endInlineKeyboard()
            ))
        }


        override fun receiveMessage(msg: String) {
            if (msg !in myMemes) {
                registerToken(channel.sendMessage(
                    Message("В вашей коллекции нет мема с таким тегом")
                        .inlineKeyboard()
                        .row().column("Отмена") { cancel("Удаление отменено") }.endRow().endInlineKeyboard()
                ))
                return
            }
            channel.sendMessage("Готово")
            complete(msg)
        }
    }

}