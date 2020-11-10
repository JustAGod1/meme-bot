package ru.justagod.bot.utility

import java.time.Duration

class ScheduledThread(name: String, private val interval: Duration, private val block: () -> Unit) {

    private val thread = Thread(::run, name)

    @Volatile
    private var killed = false


    init {
        thread.start()
    }

    private fun run() {
        while (!killed) {
            block()
            Thread.sleep(interval.toMillis())
        }
    }

    fun wakeUp() {
        thread.interrupt()
    }

    fun kill() {
        killed = true
        wakeUp()
    }
}