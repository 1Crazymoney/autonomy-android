/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local

import com.bitmark.autonomy.data.ext.fromJson
import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.ResourceData
import com.bitmark.autonomy.data.source.local.api.DatabaseApi
import com.bitmark.autonomy.data.source.local.api.FileStorageApi
import com.bitmark.autonomy.data.source.local.api.SharedPrefApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class ResourceLocalDataSource @Inject constructor(
    databaseApi: DatabaseApi,
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : LocalDataSource(databaseApi, sharedPrefApi, fileStorageApi) {

    fun saveResources(poiId: String, res: List<ResourceData>) =
        Single.fromCallable {
            newGsonInstance().toJson(res).toByteArray(Charsets.UTF_8)
        }.subscribeOn(Schedulers.io()).flatMapCompletable { bytes ->
            fileStorageApi.rxCompletable { fileStorageGateway ->
                fileStorageGateway.saveOnFilesDir("cached_resources_$poiId.json", bytes)
            }
        }

    fun listResource(poiId: String) = fileStorageApi.rxMaybe { fileStorageGateway ->
        val json =
            fileStorageGateway.readOnFilesDir("cached_resources_$poiId.json")
                .toString(Charsets.UTF_8)
        newGsonInstance().fromJson<List<ResourceData>>(json)
    }.subscribeOn(Schedulers.io())

}