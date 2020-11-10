package ru.justagod.bot.base.communications

class InlineKeyboardButton(val text: String, val callback: InlineKeyboardButtonCallback) {

    constructor(text: String, callback: () -> Unit): this(text, object : InlineKeyboardButtonCallback {
        override fun onClick(token: MessageToken, userId: Long) : String? {
            callback.invoke()
            return null
        }
    })

    interface InlineKeyboardButtonCallback {
        fun onClick(token: MessageToken, userId: Long): String?
    }

    class PrivateButtonCallback(private val userId: Long, private val block: (token: MessageToken) -> String?) : InlineKeyboardButtonCallback {
        override fun onClick(token: MessageToken, userId: Long): String? {
            if (this.userId != userId) return "Вам это действие недоступно"
            return block(token)
        }

    }

}