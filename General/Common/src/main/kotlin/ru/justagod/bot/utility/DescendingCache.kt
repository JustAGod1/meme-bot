package ru.justagod.bot.utility

import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class DescendingCache<Key, Value>(
    private val descendingInterval: Duration,
    updateInterval: Duration = Duration.of(1, ChronoUnit.MINUTES)
) {

    private val heap = PriorityQueue<Pair<Instant, Key>>(kotlin.Comparator { a, b-> a.first.compareTo(b.first) })
    private val storage = hashMapOf<Key, Value>()

    private val cleanerThread = ScheduledThread("Descending cache's cleaner", updateInterval, this::cleanEntries)

    private var locked = false
    private val lock = Any()

    fun lock() {
        inMutex { locked = true }
    }

    fun unlock() {
        locked = false
    }


    fun obtainValue(key: Key, producer: () -> Value): Value {
        return inMutex {
            updateKey(key)
            storage.computeIfAbsent(key) { producer() }
        }
    }

    fun putValue(key: Key, value: Value) {
        inMutex {
            updateKey(key)
            storage[key] = value
        }
    }

    fun getValue(key: Key): Value? {
        return inMutex {
            storage[key]
        }
    }

    private fun updateKey(key: Key) {
        if (key in storage) {
            heap.removeIf { it.second == key }
        }
        heap += Instant.now() to key
    }

    private fun <T>inMutex(block: () -> T): T {
        return if (locked) block()
        else synchronized(lock, block)
    }

    private fun cleanEntries() {
        if (locked) return
        synchronized(lock) {
            val now = Instant.now()
            while (heap.isNotEmpty() && heap.peek().first < now - descendingInterval) {
                val entry = heap.poll()
                storage -= entry.second
            }
        }
    }

}