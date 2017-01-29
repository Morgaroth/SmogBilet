package io.github.morgaroth.smogbilet

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

data class Info(val date: Calendar, val text: String)

class MainActivity : AppCompatActivity(), AnkoLogger {

    val TAG = "Main"
    val url = "http://kmkrakow.pl/"

    var infoView: TextView? = null
    var info2View: TextView? = null
    var button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        infoView = findViewById(R.id.free) as TextView?
        info2View = findViewById(R.id.date) as TextView?
        button = findViewById(R.id.refresh) as Button?


        val now = GregorianCalendar()
        (findViewById(R.id.today) as TextView).text = renderDate(now)
        button?.setOnClickListener { check() }
        check()
    }

    private fun renderDate(now: Calendar) = String.format("%1\$tA %1\$td %1\$tb %1\$tY", now)

    fun parseDate(str: String): Calendar? {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
        try {
            val time = format.parse(str)
            val cal = Calendar.getInstance()
            cal.time = time
            return cal
        } catch (e: Exception) {
            warn("ex ${e.message}")
            return null
        }
    }

    fun isNotFuture(cal: Calendar): Boolean {
        val now = Calendar.getInstance()
        return cal.before(now)
    }

    fun check() {
        button?.text = getString(R.string.checking_in_progress)
        doAsync {
            info("Checking...")
            val doc = Jsoup.connect(url).get()
            val messages = doc.select(".clearBoth").toList().map {
                val text = it.select(".tytul").text()
                if (text.contentEquals("Darmowa komunikacja miejska dla kierowców")) {
                    val date = parseDate(it.select(".przedzial").text())
                    if (date != null) Info(date, text) else null
                } else null
            }.filter { it != null }.map { it!! }

            val free = messages
                    .filter { isNotFuture(it.date) }
                    .sortedByDescending { it.date.timeInMillis }

            val msg = if (free.filter { DateUtils.isToday(it.date.timeInMillis) }.isNotEmpty()) R.string.yes else R.string.no
            val lastKnown = if (free.isNotEmpty()) renderDate(free.first().date) else getString(R.string.no_info)

            uiThread {
                infoView?.text = getString(msg)
                info2View?.text = lastKnown
                button?.text = getString(R.string.refresh)
            }
            info("Checked.")
        }
    }
}
