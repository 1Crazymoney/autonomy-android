/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.splash.SplashActivity
import com.bitmark.autonomy.util.ext.getResIdentifier
import com.bitmark.autonomy.util.ext.getString
import com.bitmark.autonomy.util.isAboveP

class NotificationHelper {

    companion object {

        fun buildNotificationBundle(
            context: Context,
            @StringRes title: Int,
            @StringRes message: Int,
            @ColorRes color: Int = R.color.colorAccent,
            notificationId: Int = 0,
            headup: Boolean = false,
            channelId: String? = null,
            receiver: Class<*> = SplashActivity::class.java
        ): Bundle {
            return buildNotificationBundle(
                context.getString(title),
                context.getString(message),
                context.getColor(color),
                notificationId,
                headup,
                channelId,
                receiver
            )
        }

        fun buildNotificationBundle(
            title: String,
            message: String,
            @ColorInt color: Int,
            notificationId: Int = 0,
            headup: Boolean = false,
            channelId: String? = null,
            receiver: Class<*> = SplashActivity::class.java
        ): Bundle {
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("message", message)
            bundle.putString("receiver", receiver.name)
            bundle.putInt("color", color)
            bundle.putInt("notification_id", notificationId)
            bundle.putBoolean("head_up", headup)
            if (channelId != null) bundle.putString("channel", channelId)
            return bundle
        }

        fun buildProgressNotificationBundle(
            title: String,
            message: String,
            @ColorInt color: Int,
            notificationId: Int,
            channelId: String? = null,
            receiver: Class<*> = SplashActivity::class.java,
            maxProgress: Int = -1,
            currentProgress: Int = -1
        ): Bundle {
            val bundle =
                buildNotificationBundle(
                    title,
                    message,
                    color,
                    notificationId,
                    false,
                    channelId,
                    receiver
                )
            bundle.putBoolean("progress", true)
            bundle.putInt("max_progress", maxProgress)
            bundle.putInt("current_progress", currentProgress)
            return bundle
        }

        private fun buildNotification(context: Context, bundle: Bundle): Notification {
            val receiver = try {
                val receiverName = bundle.getString("receiver") ?: SplashActivity::class.java.name
                Class.forName(receiverName)
            } catch (e: Throwable) {
                SplashActivity::class.java
            }
            val intent = Intent(context, receiver)
            intent.putExtra("notification", bundle)
            intent.putExtra("direct_from_notification", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            val headup = bundle.getBoolean("head_up")
            val priority = if (headup) {
                if (isAboveP()) NotificationManager.IMPORTANCE_HIGH else Notification.PRIORITY_HIGH
            } else {
                if (isAboveP()) NotificationManager.IMPORTANCE_DEFAULT else Notification.PRIORITY_DEFAULT
            }
            val channelId =
                bundle.getString("channel") ?: context.getString(R.string.notification_channel_name)
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setContentTitle(bundle.getString("title", ""))
                .setContentText(bundle.getString("message"))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(priority)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(bundle.getString("message"))
                )

            val progress = bundle.getBoolean("progress")
            if (progress) {
                val maxProgress = bundle.getInt("max_progress")
                val currentProgress = bundle.getInt("current_progress")
                notificationBuilder.setProgress(maxProgress, currentProgress, maxProgress == -1)
            }

            val icon =
                context.getResIdentifier(bundle.getString("icon", ""), "drawable")
            notificationBuilder.setSmallIcon(if (icon != null && icon > 0) icon else R.drawable.ic_stat_onesignal_default)

            val color = try {
                bundle.getInt("color", context.getColor(R.color.colorAccent))
            } catch (e: Throwable) {
                null
            }
            if (color != null) notificationBuilder.color = color

            return notificationBuilder.build()
        }

        fun pushNotification(context: Context, bundle: Bundle) {

            val notification = buildNotification(context, bundle)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val headup = bundle.getBoolean("head_up")
            val channelId = bundle.getString("channel") ?: ChannelId.DEFAULT
            createChannel(context, channelId, headup)

            notificationManager.notify(bundle.getInt("notification_id", 0), notification)
        }

        fun createChannel(context: Context, channelId: String, importance: Boolean) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(channelId) != null) return
            val channelName = context.getString(channelId) ?: error("invalid channel name")
            val channelImportance = if (importance) {
                if (isAboveP()) NotificationManager.IMPORTANCE_HIGH else Notification.PRIORITY_HIGH
            } else {
                if (isAboveP()) NotificationManager.IMPORTANCE_DEFAULT else Notification.PRIORITY_DEFAULT
            }
            val channel = NotificationChannel(
                channelId,
                channelName,
                channelImportance
            )
            notificationManager.createNotificationChannel(channel)
        }

        fun cancelNotification(context: Context, id: Int) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id)
        }

        inline fun <reified T : BroadcastReceiver> pushScheduledNotification(
            context: Context,
            bundle: Bundle,
            triggerAtMillis: Long,
            requestCode: Int
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, T::class.java)
            intent.putExtras(bundle)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}

fun NotificationHelper.Companion.buildCheckInSurveyNotificationBundle(context: Context) =
    buildNotificationBundle(
        context,
        R.string.check_in_survey,
        R.string.how_r_u_right_now_tap_to_check_in,
        R.color.colorAccent,
        NotificationId.SURVEY,
        true,
        ChannelId.IMPORTANT_ALERT,
        SplashActivity::class.java
    )
