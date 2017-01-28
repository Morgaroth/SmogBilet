package io.github.morgaroth.smogbilet

import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.uiThread
import org.jsoup.Jsoup
import java.util.*


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
        (findViewById(R.id.today) as TextView).text = String.format("%1\$tA %1\$td %1\$tb %1\$tY", now)
        button?.setOnClickListener { check() }
        check()
    }

    fun check() {
        button?.text = getString(R.string.checking_in_progress)
        doAsync {
            info("Checking...")
            val doc = Jsoup.connect(url).get()
            val messages = doc.select(".clearBoth").toList().map {
                val date = it.select(".przedzial").text()
                val text = it.select(".tytul").text()
                info("info $date $text")
                Info(date, text)
            }
            val free = messages.filter {
                it.text.contains("Darmowa komunikacja miejska")
            }

            info("Darmowa komwunikacja $free")
            uiThread {
                if (free.isNotEmpty()) {
                    infoView?.text = getString(R.string.yes)
                    info2View?.text = free.first().date
                } else {
                    infoView?.text = getString(R.string.no)
                    info2View?.text = getString(R.string.no_info)
                }
                button?.text = getString(R.string.refresh)
            }
        }
    }
}
