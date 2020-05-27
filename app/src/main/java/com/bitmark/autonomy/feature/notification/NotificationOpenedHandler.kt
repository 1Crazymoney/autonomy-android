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
import com.bitmark.autonomy.data.ext.toStringArrayList
import com.bitmark.autonomy.feature.main.MainActivity
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
                val poiId = additionalData.optString(NotificationPayloadType.POI_ID)
                val symptoms = additionalData.optJSONArray(NotificationPayloadType.SYMPTOMS)
                val behaviors = additionalData.optJSONArray(NotificationPayloadType.BEHAVIORS)
                putString(NotificationPayloadType.NOTIFICATION_TYPE, notificationType)
                putString(NotificationPayloadType.HELP_ID, helpId)
                putString(NotificationPayloadType.POI_ID, poiId)
                if (symptoms != null) {
                    putStringArrayList(
                        NotificationPayloadType.SYMPTOMS,
                        symptoms.toStringArrayList()
                    )
                }

                if (behaviors != null) {
                    putStringArrayList(
                        NotificationPayloadType.BEHAVIORS,
                        behaviors.toStringArrayList()
                    )
                }

                val notificationId = when (notificationType) {
                    NotificationType.NEW_HELP_REQUEST -> NotificationId.NEW_HELP_REQUEST
                    NotificationType.ACCEPTED_HELP_REQUEST -> NotificationId.ACCEPTED_HELP_REQUEST
                    NotificationType.RISK_LEVEL_CHANGED -> NotificationId.RISK_LEVEL_CHANGED
                    NotificationType.ACCOUNT_SYMPTOM_FOLLOW_UP -> NotificationId.ACCOUNT_SYMPTOM_FOLLOW_UP
                    NotificationType.ACCOUNT_SYMPTOM_SPIKE -> NotificationId.ACCOUNT_SYMPTOM_SPIKE
                    NotificationType.BEHAVIOR_REPORT_ON_RISK_AREA -> NotificationId.BEHAVIOR_REPORT_ON_RISK_AREA
                    NotificationType.BEHAVIOR_REPORT_ON_SELF_HIGH_RISK -> NotificationId.BEHAVIOR_REPORT_ON_SELF_HIGH_RISK
                    else -> error("unsupported notification type")
                }
                putInt("notification_id", notificationId)
            }
        } else null
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("direct_from_notification", true)
        if (bundle != null) intent.putExtras(MainActivity.getBundle(bundle))
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP and Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }
}