package ru.justagod.mafia.game.config

interface Config<Data: Any> : ConfigNode<Data> {

    fun copyFrom(config: Config<Data>)

    fun clone(): Config<Data> {
        val new = this.javaClass.newInstance()
        new.copyFrom(this)
        return new
    }

}