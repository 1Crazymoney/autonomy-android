/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api.middleware

import android.text.TextUtils
import com.bitmark.autonomy.BuildConfig
import com.bitmark.autonomy.data.source.local.Jwt
import com.bitmark.autonomy.data.source.local.Location
import okhttp3.CacheControl
import okhttp3.Response
import java.util.concurrent.TimeUnit


class AutonomyApiInterceptor : Interceptor() {

    override fun getTag(): String? = "AutonomyApi"

    override fun intercept(chain: okhttp3.Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("Client-Type", "android")
            .addHeader("Client-Version", "${BuildConfig.VERSION_CODE}")
            .cacheControl(CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
        if (!TextUtils.isEmpty(Jwt.getInstance().token))
            builder.addHeader(
                "Authorization",
                "Bearer " + Jwt.getInstance().token
            )

        val location = Location.getInstance()
        if (location.isAvailable()) {
            builder.addHeader("Geo-Position", "${location.lat};${location.lng}")
        }

        return chain.proceed(builder.build())
    }
}