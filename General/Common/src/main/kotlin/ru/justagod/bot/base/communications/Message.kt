package ru.justagod.bot.base.communications


class Message(val text: String) {


    var inlineKeyboard: List<List<InlineKeyboardButton>>? = null
        private set
    var replyKeyboard: List<List<String>>? = null
        private set

    fun replyKeyboard() = ReplyKeyboardBuilder()

    fun replyKeyboard(keyboard: List<List<String>>): Message {
        replyKeyboard = keyboard
        return this
    }

    fun inlineKeyboard() = InlineKeyboardBuilder()

    fun inlineKeyboard(keyboard: List<List<InlineKeyboardButton>>): Message {
        inlineKeyboard = keyboard
        return this
    }

    inner class ReplyKeyboardBuilder {

        private val keyboard = arrayListOf<List<String>>()

        fun row() = ReplyKeyboardRowBuilder()

        fun row(vararg columns: String): ReplyKeyboardBuilder {
            addRow(columns.toList())
            return this
        }

        fun row(columns: List<String>): ReplyKeyboardBuilder {
            addRow(columns)
            return this
        }

        fun endReplyKeyboard(): Message {
            if (inlineKeyboard != null) error("")
            this@Message.replyKeyboard = keyboard
            return this@Message
        }

        private fun addRow(columns: List<String>): ReplyKeyboardBuilder {
            keyboard += columns
            return this
        }

        inner class ReplyKeyboardRowBuilder {
            private val columns = arrayListOf<String>()

            fun column(text: String): ReplyKeyboardRowBuilder {
                columns += text
                return this
            }

            fun endRow(): ReplyKeyboardBuilder {
                addRow(columns)
                return this@ReplyKeyboardBuilder
            }
        }
    }

    inner class InlineKeyboardBuilder {

        private val keyboard = arrayListOf<List<InlineKeyboardButton>>()

        fun row() = InlineKeyboardRowBuilder()

        fun row(vararg columns: InlineKeyboardButton): InlineKeyboardBuilder {
            addRow(columns.toList())
            return this
        }

        fun row(columns: List<InlineKeyboardButton>): InlineKeyboardBuilder {
            addRow(columns)
            return this
        }

        fun endInlineKeyboard(): Message {
            if (replyKeyboard != null) error("")
            this@Message.inlineKeyboard = keyboard
            return this@Message
        }

        private fun addRow(columns: List<InlineKeyboardButton>): InlineKeyboardBuilder {
            keyboard += columns
            return this
        }

        inner class InlineKeyboardRowBuilder {
            private val columns = arrayListOf<InlineKeyboardButton>()

            fun column(text: String, callback: InlineKeyboardButton.InlineKeyboardButtonCallback): InlineKeyboardRowBuilder {
                columns += InlineKeyboardButton(text, callback)
                return this
            }

            fun column(button: InlineKeyboardButton): InlineKeyboardRowBuilder {
                columns += button
                return this
            }

            fun column(text: String, callback: () -> Unit): InlineKeyboardRowBuilder {
                columns += InlineKeyboardButton(text, callback)
                return this
            }

            fun endRow(): InlineKeyboardBuilder {
                addRow(columns)
                return this@InlineKeyboardBuilder
            }
        }
    }

}