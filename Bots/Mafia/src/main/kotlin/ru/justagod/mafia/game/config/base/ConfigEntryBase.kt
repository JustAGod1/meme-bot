package ru.justagod.mafia.game.config.base

import ru.justagod.bot.base.communications.InlineKeyboardButton
import ru.justagod.bot.base.communications.Message
import ru.justagod.mafia.game.config.ConfigEntry
import ru.justagod.mafia.game.config.ConfigNode
import java.util.concurrent.Future

abstract class ConfigEntryBase<Data: Any> : ConfigEntry<Data> {

    protected fun Message.InlineKeyboardBuilder.addCancelButton(userId: Long, futureToCancel: Future<*>): Message.InlineKeyboardBuilder {
        return row()
            .column("Отменить", InlineKeyboardButton.PrivateButtonCallback(userId) {
                futureToCancel.cancel(true)
                null
            })
            .endRow()
    }

    override fun children(): List<ConfigNode<Data>> = emptyList()

}