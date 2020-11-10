package ru.justagod.combot.request

import jdk.nashorn.api.scripting.ScriptObjectMirror
import org.jsoup.Jsoup
import ru.justagod.combot.CBBotMain.format
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.script.ScriptEngineManager

object DataRequester {

    private val engine = ScriptEngineManager().getEngineByName("nashorn")



    fun requestEntries(start: Date?, end: Date?): List<UserEntry> {
        val doc = Jsoup.parse(makeUrl(start, end), 20_000)

        val rawData = doc.select("#container > script").first().data().replace("//.*".toRegex(), "")

        val parsed = engine.eval(rawData) as ScriptObjectMirror

        return parsed.values.map { mapIntoEntry(it as ScriptObjectMirror) }

    }

    private fun makeUrl(start: Date?, end: Date?): URL {

        val baseUrl = "https://combot.org/c/1339542251"
        if (start == null && end == null) return URL(baseUrl)

        val sb = StringBuilder(baseUrl)

        sb.append("?")

        if (start != null) {
            sb.append("start=").append(format.format(start))
        }
        if (end != null) {
            sb.append("&").append("end=").append(format.format(end))
        }

        return URL(sb.toString())
    }

    private fun mapIntoEntry(obj: ScriptObjectMirror): UserEntry {
        val name = obj["0"].toString().substringAfter(">").substringBefore("<").let {
            if (it.endsWith("&nbsp;")) it.dropLast(6) else it
        }.trim()
        val activity = (obj["1"] as String).dropLast(1).toDouble()
        val msgCount = obj["2"] as Int
        val activeDays = obj["3"] as Int
        val daysSinceJoin = (obj["4"] as Double).toInt()
        val joined = obj["5"] as String
        val lastMsg = obj["6"] as String

        return UserEntry(
            name,
            activity,
            msgCount,
            activeDays,
            daysSinceJoin,
            format.parse(joined),
            format.parse(lastMsg)
        )
    }


}