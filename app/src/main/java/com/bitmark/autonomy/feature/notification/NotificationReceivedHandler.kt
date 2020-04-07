/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.notification

import com.onesignal.OSNotification
import com.onesignal.OneSignal
import org.json.JSONObject


class NotificationReceivedHandler : OneSignal.NotificationReceivedHandler {

    private val notificationReceiveListeners = mutableListOf<NotificationReceiveListener>()

    fun addNotificationReceiveListener(listener: NotificationReceiveListener) {
        if (notificationReceiveListeners.contains(listener)) return
        notificationReceiveListeners.add(listener)
    }

    fun removeNotificationReceiveListener(listener: NotificationReceiveListener) {
        notificationReceiveListeners.remove(listener)
    }

    override fun notificationReceived(notification: OSNotification?) {
        notificationReceiveListeners.forEach { l -> l.onReceived(notification?.payload?.additionalData) }
    }

    interface NotificationReceiveListener {
        fun onReceived(data: JSONObject?)
    }
}