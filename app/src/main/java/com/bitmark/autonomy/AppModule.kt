/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy

import android.app.Application
import android.content.Context
import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.feature.notification.NotificationReceivedHandler
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.logging.SentryEventLogger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application

    @Provides
    @Singleton
    fun provideAppLifecycleHandler() = AppLifecycleHandler()

    @Provides
    @Singleton
    fun provideEventLogger(accountRepo: AccountRepository): EventLogger =
        SentryEventLogger(accountRepo)

    @Provides
    @Singleton
    fun provideConnectivityHandler(context: Context) = ConnectivityHandler(context)

    @Provides
    @Singleton
    fun provideLocationService(context: Context, logger: EventLogger) =
        LocationService(context, logger)

    @Provides
    @Singleton
    fun provideNotificationReceivedHandler() = NotificationReceivedHandler()

}