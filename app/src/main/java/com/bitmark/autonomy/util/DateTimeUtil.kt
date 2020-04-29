/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util

import android.content.Context
import com.bitmark.autonomy.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DateTimeUtil {

    companion object {

        val DEFAULT_TIME_ZONE = TimeZone.getDefault().id

        val ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"

        val ISO8601_SIMPLE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        val DATE_TIME_FORMAT_1 = "yyyy MMM dd HH:mm:ss"

        val DATE_FORMAT_1 = "yyyy MMM dd"

        val DATE_FORMAT_2 = "MMM dd"

        val TIME_FORMAT_1 = "HH:mm"

        val TIME_FORMAT_2 = "hh:mm a"

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
        ): Date? {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone(timezone)
            return formatter.parse(date)
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
            outputTimeZone: String = "UTC"
        ): String {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(inputTimeZone))
            calendar.timeInMillis = millis
            return dateToString(calendar.time, format, outputTimeZone)
        }

    }
}

fun DateTimeUtil.Companion.formatAgo(context: Context, dateString: String): String {
    val now = nowMillis()
    val dateMillis = stringToDate(dateString)!!.time
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

fun DateTimeUtil.Companion.randomNextMillisInHourRange(
    range: IntRange,
    num: Int,
    minRandomGap: Long = TimeUnit.HOURS.toMillis(1)
): List<Long> {
    if (range.first < 0 || range.last < 0 || range.first > 23 || range.last > 23 || range.first >= range.last) {
        error("invalid hour range")
    }
    val currentHour = getCurrentHour()
    val firstCal = Calendar.getInstance()
    firstCal.set(Calendar.HOUR_OF_DAY, range.first)
    firstCal.set(Calendar.MINUTE, 0)
    firstCal.set(Calendar.SECOND, 0)
    firstCal.set(Calendar.MILLISECOND, 0)
    val lastCal = Calendar.getInstance()
    lastCal.set(Calendar.MINUTE, 0)
    lastCal.set(Calendar.SECOND, 0)
    lastCal.set(Calendar.MILLISECOND, 0)
    lastCal.set(Calendar.HOUR_OF_DAY, range.last)

    if (range.last < currentHour) {
        firstCal.add(Calendar.DATE, 1)
        lastCal.add(Calendar.DATE, 1)
    } else if (range.first < currentHour) {
        firstCal.set(Calendar.HOUR_OF_DAY, currentHour)
    }

    var firstMillis = firstCal.timeInMillis
    val lastMillis = lastCal.timeInMillis
    val gap = (lastMillis - firstMillis) / num

    val result = mutableListOf<Long>()
    for (i in 0 until num) {
        val nextFirstMillis = firstMillis + gap

        var value = (firstMillis..nextFirstMillis).random()

        if (gap < minRandomGap) {
            result.add(value)
            break
        } else {
            if (result.isNotEmpty()) {
                val lastValue = result.last()
                while (value - lastValue < minRandomGap) {
                    value = (firstMillis..nextFirstMillis).random()
                }
            }
            result.add(value)
            firstMillis = nextFirstMillis
            if (firstMillis >= lastMillis) break
        }
    }

    return result
}