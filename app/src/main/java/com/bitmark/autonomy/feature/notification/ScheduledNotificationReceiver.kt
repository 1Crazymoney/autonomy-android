/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bitmark.autonomy.feature.notification.NotificationHelper.Companion.pushNotification

class ScheduledNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.extras == null) return
        val bundle = intent.extras!!
        pushNotification(context, bundle)
    }
}