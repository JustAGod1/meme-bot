package ru.justagod.meme.config

import java.sql.Connection
import java.sql.DriverManager

class MemeBotDataBase(
    val address: String,
    val database: String,
    val username: String,
    val password: String
) {

    fun createConnection(): Connection {
        return DriverManager.getConnection(
            "jdbc:mysql://" + address + "/" + database +
                    "?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC", username, password
        )
    }

    companion object {

        fun read() = MemeBotDataBase(
            System.getenv("config.db.address"),
            System.getenv("config.db.database"),
            System.getenv("config.db.username"),
            System.getenv("config.db.password")
        )

    }

}