package ru.justagod.bot.utility

class LazyThread(name: String, private val block: () -> Unit) {

    private val thread = Thread(::run, name)

    @Volatile
    private var killed = false


    init {
        thread.start()
    }

    private fun run() {
        while (!killed) {
            try {
                while (!killed) {
                    Thread.sleep(Long.MAX_VALUE)
                }
            } catch (e: InterruptedException) {
                // by design
            }
            if (killed) return

            block()
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