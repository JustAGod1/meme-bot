package ru.justagod.mafia.game.drive.narrating.misc.extension

abstract class Extensible {

    private val extensions = hashMapOf<Extension<*>, Any>()

    fun <T: Any> getExtension(extension: Extension<T>): T {
        return extensions[extension] as T
    }

    fun <T: Any> setExtension(extension: Extension<T>, value: T?): T? {
        return if (value == null) {
            extensions.remove(extension) as T?
        } else {
            extensions.put(extension, value) as T?
        }
    }

}