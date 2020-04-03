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

        val TIME_FORMAT_1 = "HH:mm"

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
            return try {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone(timezone)
                formatter.format(date)
            } catch (e: Throwable) {
                ""
            }
        }

        fun stringToDate(
            date: String,
            format: String = ISO8601_FORMAT,
            timezone: String = "UTC"
        ): Date? {
            return try {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone(timezone)
                formatter.parse(date)
            } catch (e: Throwable) {
                null
            }
        }

        fun isToday(dateString: String, timezone: String = "UTC"): Boolean {
            val thatDate = Calendar.getInstance()
            thatDate.time = stringToDate(dateString, timezone = timezone)
            val today = Calendar.getInstance()
            return today.get(Calendar.DAY_OF_YEAR) == thatDate.get(Calendar.DAY_OF_YEAR) && today.get(
                Calendar.YEAR
            ) == thatDate.get(Calendar.YEAR)
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