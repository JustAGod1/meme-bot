package ru.justagod.mafia.telegram.command

import ru.justagod.bot.base.command.Command
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.mafia.game.config.Config
import ru.justagod.mafia.game.config.ConfigNode
import ru.justagod.mafia.game.config.ConfigurationContext
import ru.justagod.mafia.game.config.base.StringConfigEntry

object CommandTestConfiguration : Command("config", "dede"){

    override fun execute(msg: String, channel: CommunicationChannel) {
        ConfigurationContext(
            "Тестовая конфигкрация",
            Lol(),
            Unit,
            channel
        ).run()
    }

    private class Lol : Config<Unit> {

        private val a = StringConfigEntry<Unit>(
            "a", "short desc a", "full desc a", null
        )

        override fun copyFrom(config: Config<Unit>) {
            TODO("Not yet implemented")
        }

        override fun fullDesc(): String = "full desc"

        override fun children(): List<ConfigNode<Unit>> = listOf(a)

        override fun id(): String = "root"

        override fun name(): String = "root"
    }

}