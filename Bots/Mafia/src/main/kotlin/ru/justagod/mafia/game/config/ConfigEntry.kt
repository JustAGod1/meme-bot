package ru.justagod.mafia.game.config

import com.google.common.util.concurrent.ListenableFuture
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.mafia.future.VoidFuture

interface ConfigEntry<Data: Any> : ConfigNode<Data> {

    fun printValue(): String

    fun configure(channel: CommunicationChannel, data: Data, context: ConfigurationContext<Data>): ListenableFuture<*>
}