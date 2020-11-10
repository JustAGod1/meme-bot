package ru.justagod.combot.request

import java.util.*

class UserEntry(
    val name: String,
    val activity: Double,
    val msgCount: Int,
    val activeDays: Int,
    val daysSinceJoin: Int,
    val joined: Date,
    val lastMsg: Date
)