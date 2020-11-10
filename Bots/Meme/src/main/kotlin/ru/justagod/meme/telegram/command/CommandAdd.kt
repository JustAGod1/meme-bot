package ru.justagod.meme.telegram.command

import ru.justagod.bot.base.command.Command
import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.meme.MemeBotMain
import ru.justagod.meme.data.Defender
import ru.justagod.meme.telegram.MemeBot
import ru.justagod.meme.telegram.dialog.NewMemeDialog

object CommandAdd : Command("add", "Добавить новый мем"){
    override fun execute(msg: String, channel: CommunicationChannel) {
        if (!Defender.canAddMeme(channel.userId)) {
            channel.sendMessage(Message("Вы исчерпали лимит мемов на день"))
            MemeBotMain.logger.info("${channel.userId} tried to add meme but its over limit")
            return
        }
        MemeBotMain.logger.info("User ${channel.userId} started new meme dialog" )
        MemeBot.startDialog(channel.userId, NewMemeDialog(channel.userId))
    }



}