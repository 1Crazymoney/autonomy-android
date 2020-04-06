/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bitmark.autonomy.feature.splash.SplashActivity
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal
import javax.inject.Inject


class NotificationOpenedHandler @Inject constructor(private val context: Context) :
    OneSignal.NotificationOpenedHandler {

    override fun notificationOpened(result: OSNotificationOpenResult?) {
        val additionalData = result?.notification?.payload?.additionalData
        val bundle = if (additionalData != null) {
            Bundle().apply {
                val notificationType =
                    additionalData.optString(NotificationPayloadType.NOTIFICATION_TYPE)
                val helpId = additionalData.optString(NotificationPayloadType.HELP_ID)
                putString(NotificationPayloadType.NOTIFICATION_TYPE, notificationType)
                putString(NotificationPayloadType.HELP_ID, helpId)
            }
        } else null
        val intent = Intent(context, SplashActivity::class.java)
        if (bundle != null) intent.putExtra("notification", bundle)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}