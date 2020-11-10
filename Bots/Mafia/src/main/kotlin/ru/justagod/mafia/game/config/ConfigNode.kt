package ru.justagod.mafia.game.config

interface ConfigNode<Data: Any> {

    fun fullDesc(): String

    fun children(): List<ConfigNode<Data>>

    fun name(): String

    fun id(): String
}