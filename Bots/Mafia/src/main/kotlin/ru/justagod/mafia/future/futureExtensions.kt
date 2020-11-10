package ru.justagod.mafia.future

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

fun ListenableFuture<*>.addSuccessCallback(callback: () -> Unit) {
    Futures.addCallback(
        this,
        object : FutureCallback<Any> {
            override fun onSuccess(result: Any?) {
                callback()
            }

            override fun onFailure(t: Throwable) {
            }
        }, MoreExecutors.directExecutor()
    )
}
fun <V, F: ListenableFuture<V>>F.addSuccessCallback(callback: (V?) -> Unit) {
    Futures.addCallback(
        this,
        object : FutureCallback<V> {
            override fun onSuccess(result: V?) {
                callback(result)
            }

            override fun onFailure(t: Throwable) {
            }
        }, MoreExecutors.directExecutor()
    )
}