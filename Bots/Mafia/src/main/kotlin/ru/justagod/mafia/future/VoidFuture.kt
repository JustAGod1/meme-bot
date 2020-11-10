package ru.justagod.mafia.future

import com.google.common.util.concurrent.AbstractFuture

class VoidFuture : AbstractFuture<Unit>() {

    fun markDone() {
        set(Unit)
    }

    fun markDoneWhenDone(future: VoidFuture) {
        future.addSuccessCallback { this.markDone() }
    }
}