/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bitmark.autonomy.logging.Tracer
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.randomNextMillisInHourRange
import java.util.*


class DailyPushNotificationBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val REQUEST_CODE = 0xAB

        private const val TAG = "DailyNotification"

        fun trigger(context: Context, atMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyPushNotificationBroadcastReceiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                atMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Tracer.DEBUG.log(TAG, "push survey notification at: ${Date(atMillis)}")
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val randomMillis = DateTimeUtil.randomNextMillisInHourRange(
            NotificationConstants.NOTIFICATION_HOUR_RANGE,
            NotificationConstants.PUSH_COUNT_PER_DAY
        )

        for (triggerMillis in randomMillis) {
            val bundle = NotificationHelper.buildCheckInSurveyNotificationBundle(context!!)
            NotificationHelper.pushScheduledNotification<ScheduledNotificationReceiver>(
                context,
                bundle,
                triggerMillis,
                NotificationId.SURVEY
            )
            Tracer.DEBUG.log(TAG, "push survey notification at: ${Date(triggerMillis)}")
        }
    }
}