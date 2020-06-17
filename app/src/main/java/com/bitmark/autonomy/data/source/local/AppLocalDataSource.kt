/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local

import com.bitmark.autonomy.data.source.local.api.DatabaseApi
import com.bitmark.autonomy.data.source.local.api.FileStorageApi
import com.bitmark.autonomy.data.source.local.api.SharedPrefApi
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class AppLocalDataSource @Inject constructor(
    databaseApi: DatabaseApi,
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : LocalDataSource(databaseApi, sharedPrefApi, fileStorageApi) {

    fun checkDebugModeEnable() = sharedPrefApi.rxSingle { sharedPrefGateway ->
        sharedPrefGateway.get(
            SharedPrefApi.DEBUG_MODE,
            Boolean::class
        )
    }

    fun saveDebugModeState(enable: Boolean) = sharedPrefApi.rxCompletable { sharedPrefGateway ->
        sharedPrefGateway.put(SharedPrefApi.DEBUG_MODE, enable)
    }

    fun deleteSharePref() = sharedPrefApi.rxCompletable { sharedPrefGateway ->
        sharedPrefGateway.clear()
    }.subscribeOn(Schedulers.io())

    fun deleteCache() = fileStorageApi.rxCompletable { fileStorageGateway ->
        fileStorageGateway.delete(fileStorageGateway.filesDir().absolutePath, "cached_resources")
        fileStorageGateway.delete(fileStorageGateway.filesDir().absolutePath, "cached_symptom")
        fileStorageGateway.delete(fileStorageGateway.filesDir().absolutePath, "cached_behavior")
        Jwt.getInstance().clear()
    }.subscribeOn(Schedulers.io())
}