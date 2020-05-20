/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.auth.ServerAuthentication
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer


class MainActivityViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val appRepo: AppRepository,
    private val accountRepo: AccountRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val serverAuth: ServerAuthentication
) : BaseViewModel(lifecycle) {

    internal val checkDebugModeEnableLiveData = CompositeLiveData<Boolean>()

    internal val updateTimezoneLiveData = CompositeLiveData<Any>()

    internal val updateLocationLiveData = CompositeLiveData<Any>()

    fun checkDebugModeEnable() {
        checkDebugModeEnableLiveData.add(rxLiveDataTransformer.single(appRepo.checkDebugModeEnable()))
    }

    fun updateTimezone(timezone: String) {
        updateTimezoneLiveData.add(
            rxLiveDataTransformer.completable(
                accountRepo.updateMetadata(mapOf("timezone" to timezone))
                    .andThen(accountRepo.syncAccountData().ignoreElement())
            )
        )
    }

    fun updateLocation() {
        updateLocationLiveData.add(
            rxLiveDataTransformer.completable(
                userRepo.updateLocation()
            )
        )
    }

    override fun onCreate() {
        super.onCreate()
        serverAuth.start()
    }

    override fun onDestroy() {
        serverAuth.stop()
        super.onDestroy()
    }
}