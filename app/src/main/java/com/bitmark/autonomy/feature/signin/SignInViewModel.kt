/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.signin

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.cryptography.crypto.Sha3256
import com.bitmark.cryptography.crypto.encoder.Hex.HEX
import com.bitmark.cryptography.crypto.encoder.Raw.RAW
import com.bitmark.sdk.features.Account
import com.onesignal.OneSignal
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.identity.Registration
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class SignInViewModel(
    lifecycle: Lifecycle,
    private val accountRepo: AccountRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val prepareDataLiveData = CompositeLiveData<Any>()

    fun prepareData(account: Account, keyAlias: String, authRequired: Boolean) {
        prepareDataLiveData.add(
            rxLiveDataTransformer.completable(
                prepareDataStream(
                    account,
                    keyAlias,
                    authRequired
                )
            )
        )
    }

    private fun prepareDataStream(
        account: Account,
        keyAlias: String,
        authRequired: Boolean
    ): Completable {
        val syncAccountStream = Single.fromCallable {
            val requester = account.accountNumber
            val timestamp = System.currentTimeMillis().toString()
            val signature = HEX.encode(account.sign(RAW.decode(timestamp)))
            Triple(requester, timestamp, signature)
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io()).flatMapCompletable { t ->
            val requester = t.first
            val timestamp = t.second
            val signature = t.third
            accountRepo.registerServerJwt(timestamp, signature, requester)
        }.andThen(accountRepo.syncAccountData().flatMap { accountData ->
            accountData.keyAlias = keyAlias
            accountData.authRequired = authRequired
            accountRepo.saveAccountData(accountData)
                .andThen(Single.just(accountData.accountNumber))
        })

        val registerIntercomStream = fun(id: String) = Completable.fromAction {
            val registration = Registration.create().withUserId(id)
            Intercom.client().registerIdentifiedUser(registration)
        }.subscribeOn(Schedulers.io())

        val registerNotificationStream = fun(accountNumber: String) = Completable.create { emt ->
            try {
                val tag = "account_number"
                OneSignal.getTags { tags ->
                    if (tags?.has(tag) == true) {
                        OneSignal.deleteTag(tag)
                    }
                    OneSignal.sendTag(tag, accountNumber)
                    emt.onComplete()
                }
            } catch (e: Throwable) {
                emt.onError(e)
            }
        }.subscribeOn(Schedulers.io())

        val revertDataStream = accountRepo.clearAccountData()

        return syncAccountStream
            .flatMapCompletable { accountNumber ->
                val intercomId =
                    "Autonomy_android_%s".format(Sha3256.hash(RAW.decode(accountNumber)))
                Completable.mergeArray(
                    registerIntercomStream(intercomId),
                    registerNotificationStream(accountNumber)
                )
            }.onErrorResumeNext { e ->
                revertDataStream.andThen(Completable.error(e))
            }
    }

}