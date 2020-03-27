/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.notification

import android.content.Context
import android.content.Intent
import com.bitmark.autonomy.feature.splash.SplashActivity
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal
import javax.inject.Inject


class NotificationOpenedHandler @Inject constructor(private val context: Context) :
    OneSignal.NotificationOpenedHandler {

    override fun notificationOpened(result: OSNotificationOpenResult?) {
        val intent = Intent(context, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}