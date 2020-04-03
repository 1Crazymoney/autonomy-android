/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local

import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.AccountData
import com.bitmark.autonomy.data.model.newInstance
import com.bitmark.autonomy.data.source.local.api.DatabaseApi
import com.bitmark.autonomy.data.source.local.api.FileStorageApi
import com.bitmark.autonomy.data.source.local.api.SharedPrefApi
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AccountLocalDataSource @Inject constructor(
    databaseApi: DatabaseApi,
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : LocalDataSource(databaseApi, sharedPrefApi, fileStorageApi) {

    fun saveAccountData(accountData: AccountData) =
        sharedPrefApi.rxCompletable { sharedPrefGateway ->
            sharedPrefGateway.put(SharedPrefApi.ACCOUNT_DATA, newGsonInstance().toJson(accountData))
        }

    fun getAccountData(): Single<AccountData> = sharedPrefApi.rxSingle { sharedPrefGateway ->
        val accountData = newGsonInstance().fromJson(
            sharedPrefGateway.get(SharedPrefApi.ACCOUNT_DATA, String::class),
            AccountData::class.java
        )
        accountData ?: AccountData.newInstance()
    }

    fun clearAccountData() = sharedPrefApi.rxCompletable { sharedPrefGateway ->
        sharedPrefGateway.clear(SharedPrefApi.ACCOUNT_DATA)
    }

    fun checkJwtExpired() = Single.fromCallable {
        System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5) > Jwt.getInstance().expiredAt
    }
}