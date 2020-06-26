/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util

import android.content.Context
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.trending.Period
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateTimeUtil {

    companion object {

        val ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"

        val ISO8601_SIMPLE_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        val ISO8601_SIMPLE_FORMAT_2 = "yyyy-MM-dd'T'HH:mm:ss"

        val DATE_TIME_FORMAT_1 = "yyyy MMM dd HH:mm:ss"

        val DATE_FORMAT_1 = "yyyy MMM dd"

        val DATE_FORMAT_2 = "MMM dd"

        val DATE_FORMAT_3 = "dd"

        val DATE_FORMAT_4 = "MMM"

        val DATE_FORMAT_5 = "yyyy MMM"

        val DATE_FORMAT_6 = "yyyy"

        val DATE_FORMAT_7 = "yyyy-MM-dd"

        val DATE_FORMAT_8 = "yyyy-MM"

        val DATE_FORMAT_9 = "EEEEE"

        val TIME_FORMAT_1 = "HH:mm"

        val TIME_FORMAT_2 = "hh:mm a"

        fun getDefaultTimezoneId(): String = TimeZone.getDefault().id

        fun getDefaultTimezone(): String {
            val calendar = GregorianCalendar()
            val timezone = calendar.timeZone
            val offset =
                timezone.rawOffset + if (timezone.inDaylightTime(Date())) timezone.dstSavings else 0
            val gmtOffset = TimeUnit.HOURS.convert(offset.toLong(), TimeUnit.MILLISECONDS)
            return if (gmtOffset >= 0) "GMT+$gmtOffset" else "GMT$gmtOffset"
        }

        fun getTimeZoneOffset(timezoneId: String): String {
            val calendar = GregorianCalendar()
            calendar.timeZone = TimeZone.getTimeZone(timezoneId)
            val timezone = calendar.timeZone
            val offset =
                timezone.rawOffset + if (timezone.inDaylightTime(Date())) timezone.dstSavings else 0
            val gmtOffset = TimeUnit.HOURS.convert(offset.toLong(), TimeUnit.MILLISECONDS)
            return if (gmtOffset >= 0) "+%02d:00".format(gmtOffset) else "%02d:00".format(gmtOffset)
        }

        fun stringToString(
            date: String,
            oldFormat: String = ISO8601_FORMAT,
            newFormat: String = DATE_TIME_FORMAT_1,
            oldTimezone: String = "UTC",
            newTimezone: String = "UTC"
        ): String {
            return try {
                var formatter = SimpleDateFormat(oldFormat, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone(oldTimezone)
                val d = formatter.parse(date)
                formatter = SimpleDateFormat(newFormat, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone(newTimezone)
                formatter.format(d)
            } catch (e: Throwable) {
                ""
            }

        }

        fun now(format: String, timezone: String = "UTC") =
            dateToString(Calendar.getInstance().time, format, timezone)

        fun nowMillis() = Calendar.getInstance().timeInMillis

        fun dateToString(
            date: Date,
            format: String = ISO8601_FORMAT,
            timezone: String = "UTC"
        ): String {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone(timezone)
            return formatter.format(date)
        }

        fun stringToDate(
            date: String,
            format: String = ISO8601_FORMAT,
            timezone: String = "UTC"
        ): Date {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone(timezone)
            return formatter.parse(date) ?: error("cannot parse date")
        }

        fun isToday(dateString: String, timezone: String = "UTC"): Boolean {
            val thatDate = Calendar.getInstance()
            thatDate.time = stringToDate(dateString, timezone = timezone)
            val today = Calendar.getInstance()
            return today.get(Calendar.DAY_OF_YEAR) == thatDate.get(Calendar.DAY_OF_YEAR) && today.get(
                Calendar.YEAR
            ) == thatDate.get(Calendar.YEAR)
        }

        fun calculateGapMillisTo(hour: Int): Long {
            if (hour < 0 || hour > 23) error("invalid hour value")
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            if (hour < currentHour) {
                calendar.add(Calendar.DATE, 1)
            }
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis - now
        }

        fun getCurrentHour() = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        fun millisToString(
            millis: Long,
            format: String,
            inputTimeZone: String = "UTC",
            outputTimeZone: String = "UTC",
            includeTimezoneOffset: Boolean = false
        ): String {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(inputTimeZone))
            calendar.timeInMillis = millis
            val result = dateToString(calendar.time, format, outputTimeZone)
            return if (includeTimezoneOffset) result + getTimeZoneOffset(outputTimeZone) else result
        }

        fun getStartOfThisWeekMillis(timezone: String = "UTC") =
            getStartOfWeekMillis(Calendar.getInstance().timeInMillis, 0, timezone)

        fun getStartOfWeekMillis(thisWeekMillis: Long, gap: Int, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = thisWeekMillis
            calendar.add(Calendar.DAY_OF_YEAR, gap * 7)
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val startOfSunday = getStartOfDate(calendar)
            return startOfSunday.timeInMillis
        }

        fun getStartOfDate(calendar: Calendar): Calendar {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar
        }

        fun getStartOfThisYearMillis(timezone: String = "UTC"): Long =
            getStartOfYearMillis(Calendar.getInstance().timeInMillis, 0, timezone)

        fun getStartOfYearMillis(thisYearMillis: Long, gap: Int, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = thisYearMillis
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.add(Calendar.YEAR, gap)
            val startOfYear = getStartOfDate(calendar)
            return startOfYear.timeInMillis
        }

        fun getStartOfThisMonthMillis(timezone: String = "UTC") =
            getStartOfMonthMillis(Calendar.getInstance().timeInMillis, 0, timezone)

        fun getStartOfMonthMillis(thisMonthMillis: Long, gap: Int, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = thisMonthMillis
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, gap)
            val startOfYear = getStartOfDate(calendar)
            return startOfYear.timeInMillis
        }

        fun getDateRangeOfWeek(
            weekMillis: Long,
            timezone: String = "UTC"
        ) = Pair(
            Date(getStartOfWeekMillis(weekMillis, 0, timezone)),
            Date(getEndOfWeekMillis(weekMillis, timezone))
        )

        fun getEndOfWeekMillis(millis: Long, timezone: String = "UTC"): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = millis
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return getEndOfDate(calendar).timeInMillis
        }

        fun getEndOfYearMillis(millis: Long, timezone: String = "UTC"): Long {
            val nextYearMillis = getStartOfYearMillis(millis, 1, timezone)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = nextYearMillis
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return getEndOfDate(calendar).timeInMillis
        }

        fun getEndOfMonthMillis(millis: Long, timezone: String = "UTC"): Long {
            val nextMonthMillis = getStartOfMonthMillis(millis, 1, timezone)
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.timeInMillis = nextMonthMillis
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            return getEndOfDate(calendar).timeInMillis
        }

        fun getEndOfDate(calendar: Calendar): Calendar {
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar
        }

        fun getYear(date: Date, timezone: String = "UTC"): Int {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.time = date
            return calendar.get(Calendar.YEAR)
        }

        fun getMonth(date: Date, timezone: String = "UTC"): Int {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
            calendar.time = date
            return calendar.get(Calendar.MONTH)
        }

        fun listDoM() = mutableListOf<Int>().apply {
            val calendar = Calendar.getInstance()
            val maxDoM = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (i in 1..maxDoM) {
                add(i)
            }
        }.toList()

        fun getDoM(
            dateString: String,
            format: String = ISO8601_FORMAT,
            timezone: String = "UTC"
        ): Int {
            val date = stringToDate(dateString, format, timezone)
            val c = Calendar.getInstance()
            c.time = date
            return c.get(Calendar.DAY_OF_MONTH)
        }

        fun getDoW(
            dateString: String,
            format: String = ISO8601_FORMAT,
            timezone: String = "UTC"
        ): Int {
            val date = stringToDate(dateString, format, timezone)
            val c = Calendar.getInstance()
            c.time = date
            return c.get(Calendar.DAY_OF_WEEK)
        }

        fun getMoY(
            dateString: String,
            format: String = ISO8601_FORMAT,
            timezone: String = "UTC"
        ): Int {
            val date = stringToDate(dateString, format, timezone)
            val c = Calendar.getInstance()
            c.time = date
            return c.get(Calendar.MONTH)
        }

    }
}

fun DateTimeUtil.Companion.formatAgo(context: Context, dateString: String): String {
    val now = nowMillis()
    val dateMillis = stringToDate(dateString).time
    val gap = now - dateMillis
    return when {
        gap > TimeUnit.DAYS.toMillis(1) -> {
            val days = gap / TimeUnit.DAYS.toMillis(1)
            String.format(
                context.getString(if (days > 1) R.string.days_ago_format else R.string.day_ago_format),
                days
            )
        }

        gap > TimeUnit.HOURS.toMillis(1) -> {
            val hours = gap / TimeUnit.HOURS.toMillis(1)
            String.format(
                context.getString(if (hours > 1) R.string.hours_ago_format else R.string.hour_ago_format),
                hours
            )
        }

        gap > TimeUnit.MINUTES.toMillis(1) -> {
            val mins = gap / TimeUnit.MINUTES.toMillis(1)
            String.format(
                context.getString(if (mins > 1) R.string.mins_ago_format else R.string.min_ago_format),
                mins
            )
        }

        else -> {
            context.getString(R.string.just_now)
        }
    }

}

fun DateTimeUtil.Companion.formatPeriod(
    period: Int,
    startedTimeMillis: Long,
    timezone: String = "UTC"
): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
    calendar.timeInMillis = startedTimeMillis
    return when (period) {
        Period.WEEK -> {
            val firstDoW = calendar.time
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val lastDoW = calendar.time
            "%s %s-%s".format(
                dateToString(firstDoW, DATE_FORMAT_4, timezone),
                dateToString(firstDoW, DATE_FORMAT_3, timezone),
                dateToString(lastDoW, DATE_FORMAT_3, timezone)
            )
        }
        Period.MONTH -> dateToString(calendar.time, DATE_FORMAT_5, timezone)
        Period.YEAR -> dateToString(calendar.time, DATE_FORMAT_6, timezone)
        else -> error("unsupported period")
    }
}