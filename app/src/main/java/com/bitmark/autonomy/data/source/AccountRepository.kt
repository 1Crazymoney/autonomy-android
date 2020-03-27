/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.model.AccountData
import com.bitmark.autonomy.data.model.mergeWith
import com.bitmark.autonomy.data.source.local.AccountLocalDataSource
import com.bitmark.autonomy.data.source.remote.AccountRemoteDataSource
import io.reactivex.Single
import io.reactivex.functions.BiFunction


class AccountRepository(
    private val remoteDataSource: AccountRemoteDataSource,
    private val localDataSource: AccountLocalDataSource
) : Repository {

    fun registerServerAccount(
        timestamp: String,
        signature: String,
        requester: String,
        encPubKey: String,
        metadata: Map<String, String>
    ) = registerServerJwt(
        timestamp,
        signature,
        requester
    ).andThen(remoteDataSource.registerServerAccount(encPubKey, metadata))

    fun registerServerJwt(
        timestamp: String,
        signature: String,
        requester: String
    ) = remoteDataSource.registerServerJwt(
        timestamp,
        signature,
        requester
    )

    fun saveAccountData(accountData: AccountData) =
        localDataSource.saveAccountData(accountData)

    fun getAccountData() = localDataSource.getAccountData()

    fun clearAccountData() = localDataSource.clearAccountData()

    fun syncAccountData() = localDataSource.getAccountData().flatMap { localAccount ->
        remoteDataSource.getAccountInfo().flatMap { account ->
            saveAccountData(account.mergeWith(localAccount)).andThen(Single.just(account))
        }
    }

    fun updateMetadata(metadata: Map<String, String>) = Single.zip(
        remoteDataSource.updateMetadata(metadata),
        localDataSource.getAccountData(),
        BiFunction<AccountData, AccountData, AccountData> { remoteAccountData, localAccountData ->
            remoteAccountData.mergeWith(localAccountData)
        }).flatMap { accountData ->
        saveAccountData(accountData).andThen(Single.just(accountData))
    }
}