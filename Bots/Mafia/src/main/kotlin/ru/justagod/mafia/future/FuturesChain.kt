package ru.justagod.mafia.future

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.*

@Suppress("UNCHECKED_CAST")
class FuturesChain {
    private val producers = LinkedList<() -> ListenableFuture<*>>()

    fun add(producer: () -> ListenableFuture<*>) {
        producers += producer
    }

    fun run(): VoidFuture {
        val future = VoidFuture()
        doRun(future)
        return future
    }

    private fun doRun(future: VoidFuture) {
        val producer = producers.poll()
        if (producer == null) {
            future.markDone()
            return
        }

        producer().addSuccessCallback {
            doRun(future)
        }
    }
}