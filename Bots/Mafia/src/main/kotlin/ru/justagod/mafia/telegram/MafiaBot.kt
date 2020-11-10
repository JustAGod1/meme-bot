package ru.justagod.mafia.telegram

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import ru.justagod.bot.base.BotBase
import ru.justagod.mafia.MafiaBotMain
import ru.justagod.mafia.telegram.command.CommandStatus
import ru.justagod.mafia.telegram.command.CommandTestConfiguration

class MafiaBot : BotBase() {



    init {
        registerCommand(CommandStatus)
        registerCommand(CommandTestConfiguration)
    }


    override fun getBotUsername(): String = MafiaBotMain.config.username

    override fun getBotToken(): String = MafiaBotMain.config.token




}