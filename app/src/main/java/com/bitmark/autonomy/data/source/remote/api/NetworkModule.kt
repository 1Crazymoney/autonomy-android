/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api

import android.content.Context
import com.bitmark.autonomy.BuildConfig
import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.source.remote.api.middleware.AutonomyApiInterceptor
import com.bitmark.autonomy.data.source.remote.api.service.AutonomyApi
import com.bitmark.autonomy.data.source.remote.api.service.ServiceGenerator
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import javax.inject.Singleton

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideHttpCache(context: Context) = Cache(context.filesDir, 10 * 1024 * 1024)

    @Singleton
    @Provides
    fun provideFbmServerApi(
        apiInterceptor: AutonomyApiInterceptor,
        cache: Cache
    ): AutonomyApi {
        return ServiceGenerator.createService(
            BuildConfig.AUTONOMY_API_ENDPOINT,
            AutonomyApi::class.java,
            newGsonInstance(),
            appInterceptors = listOf(apiInterceptor),
            cache = cache
        )
    }

    @Singleton
    @Provides
    fun provideAutonomyInterceptor() = AutonomyApiInterceptor()
}