/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local

import com.bitmark.autonomy.data.ext.fromJson
import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.BehaviorData
import com.bitmark.autonomy.data.source.local.api.DatabaseApi
import com.bitmark.autonomy.data.source.local.api.FileStorageApi
import com.bitmark.autonomy.data.source.local.api.SharedPrefApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class BehaviorLocalDataSource @Inject constructor(
    databaseApi: DatabaseApi,
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : LocalDataSource(databaseApi, sharedPrefApi, fileStorageApi) {

    fun saveBehaviors(p: Pair<List<BehaviorData>, List<BehaviorData>>) =
        Single.fromCallable {
            val map = mapOf(
                "official_behaviors" to p.first,
                "customized_behaviors" to p.second
            )
            newGsonInstance().toJson(map).toByteArray(Charsets.UTF_8)
        }.subscribeOn(Schedulers.io()).flatMapCompletable { bytes ->
            fileStorageApi.rxCompletable { fileStorageGateway ->
                fileStorageGateway.saveOnFilesDir("cached_behavior.json", bytes)
            }
        }

    fun listBehavior() = fileStorageApi.rxMaybe { fileStorageGateway ->
        val json =
            fileStorageGateway.readOnFilesDir("cached_behavior.json").toString(Charsets.UTF_8)
        val map = newGsonInstance().fromJson<Map<String, List<BehaviorData>>>(json)
        Pair(
            map["official_behaviors"] ?: error("missing `official_behaviors`"),
            map["customized_behaviors"] ?: error("missing `customized_behaviors`")
        )
    }.subscribeOn(Schedulers.io())
}