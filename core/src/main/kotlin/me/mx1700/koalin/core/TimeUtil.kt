package me.mx1700.koalin

import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {
    private val lastModifiedFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    init {
        lastModifiedFormat.timeZone = TimeZone.getTimeZone("GTM")
    }

    fun stringToDate(str: String): Date = lastModifiedFormat.parse(str)
    fun dateToString(date: Date) = lastModifiedFormat.format(date)!!
}