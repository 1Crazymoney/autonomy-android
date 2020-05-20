/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import javax.inject.Inject


class ChromeCustomTabServiceHandler @Inject constructor(private val context: Context) {

    companion object {
        private const val CHROME_PACKAGE = "com.android.chrome"
    }

    private var bound = false

    private var binding = false

    private var urls: Array<String>? = null

    private var callback: CustomTabsCallback? = null

    fun setCallback(callback: CustomTabsCallback) {
        this.callback = callback
    }

    fun setUrls(urls: Array<String>) {
        this.urls = urls
    }

    private val connection = object : CustomTabsServiceConnection() {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            binding = false
        }

        override fun onCustomTabsServiceConnected(name: ComponentName?, client: CustomTabsClient?) {
            bound = true
            binding = false
            client?.warmup(0)
            urls?.forEach { url ->
                val session = client?.newSession(callback)
                session?.mayLaunchUrl(Uri.parse(url), null, null)
            }
        }
    }

    fun bind(): Boolean {
        if (binding || bound) return true
        val ok = CustomTabsClient.bindCustomTabsService(context, CHROME_PACKAGE, connection)
        binding = true
        return ok
    }
}