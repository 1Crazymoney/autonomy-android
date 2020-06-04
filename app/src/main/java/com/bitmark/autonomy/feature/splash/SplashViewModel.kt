/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.splash

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.model.AccountData
import com.bitmark.autonomy.data.model.AppInfoData
import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.cryptography.crypto.encoder.Hex
import com.bitmark.cryptography.crypto.encoder.Raw
import com.bitmark.sdk.features.Account
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class SplashViewModel(
    lifeCycle: Lifecycle,
    private val accountRepo: AccountRepository,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifeCycle) {

    internal val getAccountDataLiveData = CompositeLiveData<AccountData>()

    internal val prepareDataLiveData = CompositeLiveData<Any>()

    internal val getAppInfoLiveData = CompositeLiveData<AppInfoData>()

    fun getAccountData() {
        getAccountDataLiveData.add(rxLiveDataTransformer.single(accountRepo.getAccountData()))
    }

    fun getAppInfo() {
        getAppInfoLiveData.add(rxLiveDataTransformer.single(appRepo.getAppInfo()))
    }

    fun prepareData(account: Account, timezone: String) {
        val registerJwtStream = Single.fromCallable {
            val requester = account.accountNumber
            val timestamp = System.currentTimeMillis().toString()
            val signature = Hex.HEX.encode(account.sign(Raw.RAW.decode(timestamp)))
            Triple(timestamp, signature, requester)
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io())
            .flatMapCompletable { t ->
                accountRepo.registerServerJwt(t.first, t.second, t.third)
            }

        val updateTimezoneStream =
            accountRepo.updateMetadata(mapOf("timezone" to timezone))
                .andThen(accountRepo.syncAccountData().ignoreElement())

        prepareDataLiveData.add(
            rxLiveDataTransformer.completable(
                registerJwtStream.andThen(updateTimezoneStream)
            )
        )
    }

}