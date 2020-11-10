package ru.justagod.mafia.game.drive.narrating.config.role

import ru.justagod.mafia.game.config.Config
import ru.justagod.mafia.game.config.ConfigNode
import ru.justagod.mafia.game.drive.narrating.config.MafiaContextData

class RoleConfig : Config<MafiaContextData> {
    override fun copyFrom(config: Config<MafiaContextData>) {
        TODO("Not yet implemented")
    }

    override fun fullDesc(): String =
        "Вам предлагается выбрать и настроить роли, которые будут доступны к раздаче, " +
                "а также создать свои собственные уникальные роли"

    override fun children(): List<ConfigNode<MafiaContextData>> {
        TODO("Not yet implemented")
    }

    override fun name(): String = "роли"

    override fun id(): String = "roles"
}