package ru.justagod.combot.request

class Request(
    val shownColumns: List<String>,
    val order: List<String>,
    val descendingOrder: Boolean,
    val count: Int
)