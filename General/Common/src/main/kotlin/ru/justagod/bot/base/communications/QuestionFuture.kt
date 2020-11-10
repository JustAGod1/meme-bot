package ru.justagod.bot.base.communications

import com.google.common.util.concurrent.AbstractFuture
import com.google.common.util.concurrent.MoreExecutors
import ru.justagod.bot.base.communications.question.Question

class QuestionFuture(
    private val question: Question,
    private val communicator: Communicator
) : AbstractFuture<String>() {

    fun onDone(block: (String) -> Unit) {
        addListener(Runnable {
            if (!isCancelled) block(get())
        }, MoreExecutors.directExecutor())
    }

    public override fun set(value: String?): Boolean {
        return super.set(value)
    }

    override fun afterDone() {
        communicator.dropQuestion(question)
    }

}