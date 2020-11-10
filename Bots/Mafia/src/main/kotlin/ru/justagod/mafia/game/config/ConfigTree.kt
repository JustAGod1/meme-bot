package ru.justagod.mafia.game.config

import java.lang.StringBuilder

class ConfigTree<Data : Any>(root: ConfigNode<Data>) {

    val root = ConfigTreeNode(root, null)

    fun getAt(path: String): ConfigTreeNode<Data>? {
        val splittedPath = path.split('.')
        return root.getAt(splittedPath, 0)
    }

    class ConfigTreeNode<Data: Any>(val root: ConfigNode<Data>, val parent: ConfigTreeNode<Data>?) {
        private val children = root.children().map { it.id() to ConfigTreeNode(it, this) }.toMap().toMutableMap()

        fun getAt(path: List<String>, offset: Int) : ConfigTreeNode<Data>? {
            val id = path.getOrNull(offset) ?: return null
            val child = children[id] ?: return null
            return if (offset >= path.lastIndex) child
            else child.getAt(path, offset + 1)
        }

        private fun makeHumanPath(sb: StringBuilder) {
            if (sb.isNotEmpty()) sb.insert(0, root.name() + ".")
            else sb.insert(0, root.name())
        }

        fun makeHumanPath() = buildString { makeHumanPath(this) }
    }

}