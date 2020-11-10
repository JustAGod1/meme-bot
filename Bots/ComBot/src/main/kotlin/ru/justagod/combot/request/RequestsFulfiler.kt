package ru.justagod.combot.request

import joptsimple.HelpFormatter
import joptsimple.OptionDescriptor
import joptsimple.OptionException
import joptsimple.OptionParser
import ru.justagod.combot.CBBotMain
import ru.justagod.combot.telegram.CBBot
import java.io.StringWriter
import java.lang.Exception
import java.lang.StringBuilder
import java.text.DateFormat
import java.util.*
import kotlin.Comparator

object RequestsFulfiler {

    private val fieldsAccessors = mapOf(
        "usr" to ComparableFieldAccessor { it.name},
        "act" to ActivityFieldAccessor,
        "msg" to ComparableFieldAccessor { it.msgCount },
        "ad" to ComparableFieldAccessor { it.activeDays },
        "dsj" to ComparableFieldAccessor { it.daysSinceJoin },
        "jnd" to DateFieldAccessor { it.joined },
        "lst" to DateFieldAccessor { it.lastMsg }
    )

    private val optionParser = OptionParser()

    init {
        optionParser.accepts("sort", "Sorts results by given columns").withRequiredArg().defaultsTo("act")
        optionParser.accepts("show", "Shows given columns by given order").withRequiredArg().defaultsTo("usr,act")
        optionParser.accepts("count", "Shows only that amount of rows").withRequiredArg().ofType(Int::class.java).defaultsTo(5)
        optionParser.accepts("order", "Order by desc/asc").withRequiredArg().defaultsTo("desc")
        optionParser.accepts("start", "Requests data from that date, (e.g. 2020-09-20)").withRequiredArg()
        optionParser.accepts("end", "Requests data till that date, (e.g. 2020-09-20)").withRequiredArg()
        optionParser.formatHelpWith(CBHelpFormatter)
    }


    private object CBHelpFormatter : HelpFormatter {
        override fun format(options: MutableMap<String, out OptionDescriptor>): String {
            return buildString {
                for ((name, desc) in options) {
                    if (desc.representsNonOptions()) continue
                    append(name).append(" - ").append(desc.description())
                    append('\n')
                }
            }
        }

    }

    fun fulfilRequest(cmd: String, chatId: Long, bot: CBBot) {
        val args = cmd.split("\\s+".toRegex()).toTypedArray()
        val options = try {
            optionParser.parse(*args)
        } catch (e: OptionException) {
            bot.sendSimpleMsg(chatId, "Incorrect args\n"+e.message)
            val sw = StringWriter()
            optionParser.printHelpOn(sw)
            bot.sendSimpleMsg(chatId, "```\n" + sw.buffer.toString() + "```")
            return
        }

        val sortingKeys = options.valueOf("sort").toString().split(",")
        var comparator: Comparator<UserEntry>? = null
        for (i in sortingKeys.indices.reversed()) {
            val sortingKey = sortingKeys[i]
            if (sortingKey !in fieldsAccessors.keys) {
                unrecognizedColumn(sortingKey, chatId, bot)
            }
            val accessor = fieldsAccessors[sortingKey]!!
            comparator = if (comparator == null) {
                accessor.comparator()
            } else {
                Comparator { a, b -> if (comparator!!.compare(a, b) == 0) accessor.comparator().compare(a, b) else comparator!!.compare(a, b) }
            }
        }

        if (comparator == null) {
            bot.sendSimpleMsg(chatId, "I need at least one comparator")
            return
        }

        val start = if (options.has("start")) {
            val startStr = options.valueOf("start").toString()
            try {
                CBBotMain.format.parse(startStr)
            } catch (e: Exception) {
                bot.sendSimpleMsg(chatId, "Cannot parse $startStr")
                return
            }
        } else null

        val end = if (options.has("end")) {
            val endStr = options.valueOf("end").toString()
            try {
                CBBotMain.format.parse(endStr)
            } catch (e: Exception) {
                bot.sendSimpleMsg(chatId, "Cannot parse $endStr")
                return
            }
        } else null

        val showAccessors = arrayListOf<FieldAccessor<*>>()

        val showingKeys = options.valueOf("show").toString().split(",")
        for (showingKey in showingKeys) {
            if (showingKey !in fieldsAccessors.keys) {
                unrecognizedColumn(showingKey, chatId, bot)
            }
            showAccessors += fieldsAccessors[showingKey]!!
        }


        if (showAccessors.isEmpty()) {
            bot.sendSimpleMsg(chatId, "I need at least one column to show")
            return
        }

        val entries = try {
            DataRequester.requestEntries(start, end)
        } catch (e: Exception) {
            CBBotMain.logger.error("Exception while requesting data", e)
            bot.sendSimpleMsg(chatId, "Cannot request Combot data. Please try again later.")
            return
        }.toMutableList()

        if (options.valueOf("order") == "desc") {
            entries.sortWith(comparator.reversed())
        } else {
            entries.sortWith(comparator)
        }

        val count = options.valueOf("count") as Int


        if (count < 1) {
            bot.sendSimpleMsg(chatId, "Count must be greater than 1")
            return
        }


        val result = StringBuilder("```\n")
        for (i in 0..(Math.min((count - 1), entries.lastIndex))) {
            val entry = entries[i]
            result.append(i + 1).append(") ")
            for (accessor in showAccessors) {
                result
                    .append(accessor.makeString(entry, CBBotMain.format))
                    .append(" ")
            }
            result.append('\n')
        }

        result.append("```")

        bot.sendSimpleMsg(chatId, result.toString())

    }

    private val columnsHint = buildString {
        append("Available columns:\n")
        append("usr - Username\n")
        append("act - User's activity\n")
        append("msg - Messages count\n")
        append("ad - Active days\n")
        append("dsj - Days since join\n")
        append("jnd - Date of join\n")
        append("lst - Date of last message")
    }

    private fun unrecognizedColumn(column: String, chatId: Long, bot: CBBot) {
        bot.sendSimpleMsg(chatId, "Unrecognized column: '$column'\n$columnsHint")

    }


    private interface FieldAccessor<T> {
        fun comparator(): Comparator<UserEntry>
        fun makeString(entry: UserEntry, dateFormat: DateFormat): String
    }

    private open class ComparableFieldAccessor<T: Comparable<T>>(private val getter: (UserEntry) -> T): FieldAccessor<T> {
        override fun comparator(): Comparator<UserEntry> = Comparator { a, b -> getter(a).compareTo(getter(b)) }

        override fun makeString(entry: UserEntry, dateFormat: DateFormat): String = getter(entry).toString()
    }

    private object ActivityFieldAccessor: ComparableFieldAccessor<Double>({it.activity}) {

        override fun makeString(entry: UserEntry, dateFormat: DateFormat): String = entry.activity.toString() + "%"
    }

    private class DateFieldAccessor(private val getter: (UserEntry) -> Date): ComparableFieldAccessor<Date>(getter) {

        override fun makeString(entry: UserEntry, dateFormat: DateFormat): String = dateFormat.format(getter(entry))
    }

}