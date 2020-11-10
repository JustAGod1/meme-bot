package ru.justagod.mafia.game.drive.narrating.model

import ru.justagod.mafia.game.drive.narrating.misc.extension.Extensible

class Member(val userId: Long, val role: Role) : Extensible() {

    var dead = false
}