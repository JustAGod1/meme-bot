package ru.justagod.meme.data

import ru.justagod.bot.utility.LazyThread
import ru.justagod.meme.MemeBotMain
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.LinkedBlockingQueue

object Defender {

    private const val MAX_UPLOADS_PER_DAY = 20

    private var lastUpdate = LocalDate.now()
    private val userUploads = hashMapOf<Long, Int>()

    private val savingQueue = LinkedBlockingQueue<Pair<Long, Meme>>()
    private val uploadingThread = LazyThread("Defender Saving Thread", this::uploadRecords)


    fun syncData() {
        MemeBotMain.logger.info("Starting defender synchronization...")
        val start = Instant.now()
        val connection = MemeBotMain.config.database.createConnection()
        val statement = connection.createStatement()
        val query = statement.executeQuery("SELECT COUNT(*) as `count`,userId, `time` FROM new_memes_log WHERE `time` > timestamp(current_date) GROUP BY userId")

        userUploads.clear()
        while (query.next()) {
            val count = query.getInt("count")
            val userId = query.getLong("userId")

            userUploads[userId] = count

        }

        query.close()
        statement.close()
        connection.close()
        val delta = ChronoUnit.MILLIS.between(start, Instant.now())
        MemeBotMain.logger.info("Synchronization has been completed in ${delta}ms")
    }

    private fun reSyncIfNeeded() {
        if (lastUpdate < LocalDate.now()) syncData()
    }

    fun canAddMeme(userId: Long): Boolean {
        reSyncIfNeeded()
        val committedUploads = userUploads[userId] ?: 0
        return committedUploads < MAX_UPLOADS_PER_DAY
    }

    fun addMeme(userId: Long, meme: Meme) {
        savingQueue += userId to meme
        uploadingThread.wakeUp()
        MemesStorage.addMeme(meme)
    }

    private fun uploadRecords() {
        val entries = arrayListOf<Pair<Long, Meme>>()
        while (savingQueue.isNotEmpty()) {
            entries +=savingQueue.poll()
        }
        if (entries.isEmpty()) return
        val connection = MemeBotMain.config.database.createConnection()
        val statement = connection.prepareStatement("INSERT INTO new_memes_log (userId, tag, global) VALUES (?, ?, ?)")

        var i = 0

        for ((userId, entry) in entries) {
            statement.setLong(1, userId)
            statement.setString(2, entry.tag)
            statement.setBoolean(3, entry.owner == null)

            statement.addBatch()

            i++
            if (i % 1000 == 0 || i == entries.size) {
                statement.executeBatch()
            }
        }



        statement.close()
        connection.close()
    }
}