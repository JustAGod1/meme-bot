package ru.justagod.mafia.game.config

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import ru.justagod.bot.base.communications.InlineKeyboardButton
import ru.justagod.bot.base.communications.Message
import ru.justagod.bot.base.communications.MessageToken
import ru.justagod.bot.base.communications.channel.CommunicationChannel
import ru.justagod.mafia.future.VoidFuture
import java.lang.StringBuilder

class ConfigurationContext<Data : Any>(
    private val title: String,
    private val rootConfig: Config<Data>,
    private val data: Data,
    private val channel: CommunicationChannel
) {

    private var tree = ConfigTree(rootConfig)
    private var currentConfig = tree.root
    private var path = ""

    private val tokensInUse = arrayListOf<MessageToken>()
    private var entryFuture: ListenableFuture<*>? = null

    private val myFuture = VoidFuture()

    fun run(): VoidFuture {
        printNodeInfo()

        return myFuture
    }

    private fun changeNode(newPath: String): Boolean {
        val node = if (newPath.isEmpty()) tree.root
        else (tree.getAt(newPath) ?: return false)

        when (val configNode = node.root) {
            is ConfigEntry<Data> -> {
                val future = configNode.configure(channel, data, this)
                entryFuture = future
                future.addListener(Runnable {
                    entryFuture = null
                    printNodeInfo()
                }, MoreExecutors.directExecutor())

            }
            is Config<Data> -> {
                currentConfig = node
                path = newPath
                printNodeInfo()
            }
            else -> error("What de fuck is ${node.javaClass}")
        }

        return true
    }

    private fun printNodeInfo() {
        tokensInUse += channel.sendMessage(Message(makeDescription()).inlineKeyboard(makeNavigationKeyboard()))
    }

    private fun regenTree() {
        tree = ConfigTree(rootConfig)
        currentConfig = tree.getAt(path)!!
    }

    private fun makeDescription(): String {
        val sb = StringBuilder()
        sb.append(title).append("\n\n")
        sb.append("Текущий путь: ").append(currentConfig.makeHumanPath()).append('\n')
        sb.append("\n")
        sb.append(currentConfig.root.fullDesc())
        sb.append("\n\n")

        sb.append("Доступные параметры: \n")

        val children = currentConfig.root.children()
        for ((i, child) in children.withIndex()) {
            sb.append("${i + 1}. ")
            sb.append(child.name())
            if (child is ConfigEntry<Data>) {
                sb.append(": ").append(child.printValue())
            }
            sb.append('\n')
        }

        sb.append('\n')
        sb.append("""
            Для настройки любого параметра из предложенного списка, нажмите на соответствующую кнопку.
        """.trimIndent())
        return sb.toString()
    }

    private fun makeNavigationKeyboard(): List<List<InlineKeyboardButton>> {
        val path = path
        val result = arrayListOf<List<InlineKeyboardButton>>()
        if (path.contains(".")) {
            result += listOf(
                InlineKeyboardButton(
                    "Назад",
                    InlineKeyboardButton.PrivateButtonCallback(channel.userId) {
                        tryChangeNode(path.substringBeforeLast('.'))
                    }
                )
            )
        }
        return currentConfig.root.children().chunked(2).mapTo(result) {
            it.mapIndexed { i, node ->
                InlineKeyboardButton(
                    "${i + 1}",
                    InlineKeyboardButton.PrivateButtonCallback(channel.userId) {
                        tryChangeNode(path + node.id())
                    }
                )

            }
        }
    }

    private fun tryChangeNode(path: String): String? {
        if (entryFuture != null) return "Вы заняты настройкой"
        val result = changeNode(path)
        if (!result) return "Такого пути больше не существует"

        return null
    }

    fun cleanup() {
        tokensInUse.forEach(MessageToken::deanimate)
        entryFuture?.cancel(true)
    }

}