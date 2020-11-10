package ru.justagod.meme.data

import ru.justagod.bot.utility.DescendingCache
import ru.justagod.bot.utility.LazyThread
import ru.justagod.meme.MemeBotMain
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.LinkedBlockingQueue

object MemesStorage {

    private val dbTasksQueue = LinkedBlockingQueue<SyncTask>()
    private val dbThread = LazyThread("Storage database Thread", this::uploadMemes)

    private val globalMemeTags = hashSetOf<String>()
    private val globalMemes = hashSetOf<Meme>()
    private val usersWithPrivateMemes = hashSetOf<Long>()

    private val privateMemes = DescendingCache<Long, MutableSet<Meme>>(Duration.of(10, ChronoUnit.MINUTES))
    private val privateMemesUpstreamCache = DescendingCache<Long, MutableSet<Meme>>(Duration.of(10, ChronoUnit.MINUTES))


    fun syncData() {
        MemeBotMain.logger.info("Starting db synchronization...")
        val start = Instant.now()

        val connection = MemeBotMain.config.database.createConnection()
        val statement = connection.createStatement()
        val query = statement.executeQuery("SELECT tag, fileId, `desc`, owner FROM memes")

        while (query.next()) {
            val tag = query.getString(1)
            val fileId = query.getString(2)
            val desc = query.getString(3)
            val owner = query.getBigDecimal(4)?.toLong()

            if (owner != null) {
                usersWithPrivateMemes += owner
                continue
            }

            globalMemes += Meme(fileId, tag, desc, null)
            globalMemeTags += tag
        }

        query.close()
        statement.close()
        connection.close()
        val delta = ChronoUnit.MILLIS.between(start, Instant.now())
        MemeBotMain.logger.info("Synchronization has been completed in ${delta}ms")
        MemeBotMain.logger.info("Global memes count: ${globalMemes.size}, Users with private memes count: ${usersWithPrivateMemes.size}")
    }

    fun deleteMeme(userId: Long, tag: String) {
        privateMemes.getValue(userId)?.removeIf { it.tag == tag }
        privateMemesUpstreamCache.getValue(userId)?.removeIf { it.tag == tag }

        dbTasksQueue += DeleteTask(userId, tag)

        dbThread.wakeUp()

    }

    fun hasTag(tag: String, userId: Long): Boolean {
        if (tag in globalMemeTags) return true
        if (userId !in usersWithPrivateMemes) return false


        val connection = MemeBotMain.config.database.createConnection()
        val statement = connection.prepareStatement("SELECT * FROM memes WHERE owner = ? AND tag = ? LIMIT 1")
        statement.setLong(1, userId)
        statement.setString(2, tag)
        val query = statement.executeQuery()

        val result = query.next()

        query.close()
        statement.close()
        connection.close()

        return result
    }

    fun getGlobalMemes() = globalMemes

    fun addMeme(meme: Meme) {
        if (meme.owner == null) {
            globalMemes += meme
        } else {
            privateMemesUpstreamCache.putValue(meme.owner, privateMemesUpstreamCache.obtainValue(meme.owner) { hashSetOf() }.also { it += meme })
            usersWithPrivateMemes += meme.owner
        }

        dbTasksQueue += UploadTask(meme)
        dbThread.wakeUp()
    }

    fun getPrivateMemes(userId: Long) : Set<Meme> {
        if (userId !in usersWithPrivateMemes) return emptySet()

        val general = privateMemes.obtainValue(userId) { queryPrivateMemes(userId) } as HashSet // Life hack
        val cached = privateMemesUpstreamCache.getValue(userId) ?: emptySet<Meme>()

        general.addAll(cached)

        return general
    }

    private fun queryPrivateMemes(userId: Long) : MutableSet<Meme> {
        val connection = MemeBotMain.config.database.createConnection()
        val statement = connection.prepareStatement("SELECT tag, fileId, `desc` FROM memes WHERE owner = ?")
        statement.setLong(1, userId)

        val query = statement.executeQuery()
        val result = hashSetOf<Meme>()

        while (query.next()) {
            val tag = query.getString(1)
            val fileId = query.getString(2)
            val desc = query.getString(3)

            result += Meme(fileId, tag, desc, userId)
        }

        query.close()
        statement.close()
        connection.close()

        return result
    }

    private fun uploadMemes() {
        val uploadTasks = arrayListOf<UploadTask>()
        val deleteTasks = arrayListOf<DeleteTask>()
        while (dbTasksQueue.isNotEmpty()) {
            val task = dbTasksQueue.poll()
            when (task) {
                is UploadTask -> uploadTasks += task
                is DeleteTask -> deleteTasks += task
                else -> error("")
            }
        }
        if (uploadTasks.isEmpty() && deleteTasks.isEmpty()) return
        val connection = MemeBotMain.config.database.createConnection()
        if (uploadTasks.isNotEmpty()) {
            val statement =
                connection.prepareStatement("INSERT INTO memes (tag, fileId, `desc`, owner) VALUES (?, ?, ?, ?)")

            var i = 0

            for (task in uploadTasks) {
                val entry = task.meme
                MemeBotMain.logger.info("Uploading $entry to db..")
                statement.setString(1, entry.tag)
                statement.setString(2, entry.fileId)
                statement.setString(3, entry.desc)
                statement.setBigDecimal(4, entry.owner?.let { BigDecimal(it) })

                statement.addBatch()

                i++
                if (i % 1000 == 0 || i == uploadTasks.size) {
                    statement.executeBatch()
                }
            }
            statement.close()
        }

        if (deleteTasks.isNotEmpty()) {
            val statement = connection.prepareStatement("DELETE FROM memes WHERE tag = ? AND owner = ?")

            for (task in deleteTasks) {
                MemeBotMain.logger.info("Deleting meme with tag ${task.tag} of user ${task.userId}")
                statement.setString(1, task.tag)
                statement.setBigDecimal(2, BigDecimal(task.userId))

                statement.execute()

            }
            statement.close()
        }


        connection.close()

    }

}