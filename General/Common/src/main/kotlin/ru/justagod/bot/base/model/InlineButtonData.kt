package ru.justagod.bot.base.model

import ru.justagod.bot.base.communications.InlineKeyboardButton
import ru.justagod.bot.base.communications.MessageToken

class InlineButtonData(val callback: InlineKeyboardButton.InlineKeyboardButtonCallback, val token: MessageToken) {
}