/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util

import java.text.SimpleDateFormat
import java.util.*

class DateTimeUtil {

    companion object {

        val DEFAULT_TIME_ZONE = TimeZone.getDefault().id

        val ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"

        val ISO8601_SIMPLE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

        val DATE_TIME_FORMAT_1 = "yyyy MMM dd HH:mm:ss"

        val DATE_FORMAT_1 = "yyyy MMM dd"

        val TIME_FORMAT_1 = "HH:mm"

        fun stringToString(date: String) =
            stringToString(date, DATE_TIME_FORMAT_1)

        fun stringToString(date: String, newFormat: String) =
            stringToString(date, ISO8601_FORMAT, newFormat)

        fun stringToString(
            date: String,
            oldFormat: String,
            newFormat: String,
            timezone: String = DEFAULT_TIME_ZONE
        ): String {
            return try {
                var formatter = SimpleDateFormat(oldFormat, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone(timezone)
                val d = formatter.parse(date)
                formatter = SimpleDateFormat(newFormat, Locale.getDefault())
                formatter.format(d)
            } catch (e: Throwable) {
                ""
            }

        }

        fun now(format: String, timezone: String = DEFAULT_TIME_ZONE) =
            dateToString(Calendar.getInstance().time, format, timezone)

        fun dateToString(date: Date): String =
            dateToString(date, ISO8601_FORMAT)

        fun dateToString(
            date: Date,
            format: String,
            timezone: String = DEFAULT_TIME_ZONE
        ): String {
            return try {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone(timezone)
                formatter.format(date)
            } catch (e: Throwable) {
                ""
            }
        }

        fun stringToDate(date: String) = stringToDate(date, ISO8601_FORMAT)

        fun stringToDate(
            date: String,
            format: String,
            timezone: String = DEFAULT_TIME_ZONE
        ): Date? {
            return try {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone(timezone)
                formatter.parse(date)
            } catch (e: Throwable) {
                null
            }
        }

        fun dayCountFrom(date: Date): Long {
            val nowMillis = Date().time
            val diff = nowMillis - date.time
            return diff / (1000 * 60 * 60 * 24)
        }
    }
}