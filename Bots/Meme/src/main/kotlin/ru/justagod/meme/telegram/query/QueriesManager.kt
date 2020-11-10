package ru.justagod.meme.telegram.query

import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.cached.InlineQueryResultCachedPhoto
import ru.justagod.meme.data.Meme
import ru.justagod.meme.data.MemesStorage

object QueriesManager {

    fun findMemes(prefix: String, userId: Long): List<Meme> {
        val privateMemes = MemesStorage.getPrivateMemes(userId)
        val globalMemes = MemesStorage.getGlobalMemes()

        val result = arrayListOf<Meme>()

        for (privateMeme in privateMemes) {
            if (privateMeme.tag.startsWith(prefix)) result += privateMeme
        }
        for (globalMeme in globalMemes) {
            if (globalMeme.tag.startsWith(prefix)) result += globalMeme
        }

        return result
    }

    fun makeInlineQueryResults(prefix: String, userId: Long): List<InlineQueryResultCachedPhoto> {
        val memes = findMemes(prefix, userId)

        return memes.mapIndexed { a, b -> convertToQueryResult(b, a.toString()) }
    }

    private fun convertToQueryResult(meme: Meme, id: String): InlineQueryResultCachedPhoto {
        val result = InlineQueryResultCachedPhoto()
        result.id = id
        result.title = meme.tag
        result.description = meme.desc
        result.photoFileId = meme.fileId
        result.parseMode = "Markdown"

        return result
    }

    private class QuerySession(val entries: List<Meme>)
}