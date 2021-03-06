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
import com.bitmark.autonomy.util.modelview.AreaModelView
import io.reactivex.Single


class MainActivityViewModel(
    lifecycle: Lifecycle,
    private val appRepo: AppRepository,
    private val userRepo: UserRepository,
    private val accountRepo: AccountRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val serverAuth: ServerAuthentication
) : BaseViewModel(lifecycle) {

    internal val checkDebugModeEnableLiveData = CompositeLiveData<Boolean>()

    internal val updateTimezoneLiveData = CompositeLiveData<Any>()

    internal val updateLocationLiveData = CompositeLiveData<Any>()

    internal val renameAreaLiveData = CompositeLiveData<Pair<String, String>>()

    internal val deleteAreaLiveData = CompositeLiveData<String>()

    internal val getCurrentAreaScoreLiveData = CompositeLiveData<Float>()

    internal val listAreaLiveData = CompositeLiveData<List<AreaModelView>>()

    fun checkDebugModeEnable() {
        checkDebugModeEnableLiveData.add(rxLiveDataTransformer.single(appRepo.checkDebugModeEnable()))
    }

    fun rename(id: String, name: String) {
        renameAreaLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.rename(id, name).andThen(
                    Single.just(Pair(id, name))
                )
            )
        )
    }

    fun delete(id: String) {
        deleteAreaLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.deleteArea(id).andThen(
                    Single.just(
                        id
                    )
                )
            )
        )
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

    fun getYourAutonomyScore() {
        getCurrentAreaScoreLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getAutonomyProfile(me = true).map { profile ->
                    profile.autonomyScore
                })
        )
    }

    fun listArea() {
        listAreaLiveData.add(
            rxLiveDataTransformer.single(userRepo.listArea()
                .map { areas -> areas.map { a -> AreaModelView.newInstance(a) } })
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