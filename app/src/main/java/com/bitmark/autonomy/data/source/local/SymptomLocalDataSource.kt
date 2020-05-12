/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.local

import com.bitmark.autonomy.data.ext.fromJson
import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.SymptomData
import com.bitmark.autonomy.data.source.local.api.DatabaseApi
import com.bitmark.autonomy.data.source.local.api.FileStorageApi
import com.bitmark.autonomy.data.source.local.api.SharedPrefApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class SymptomLocalDataSource @Inject constructor(
    databaseApi: DatabaseApi,
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : LocalDataSource(databaseApi, sharedPrefApi, fileStorageApi) {

    fun saveSymptoms(t: Triple<List<SymptomData>, List<SymptomData>, List<SymptomData>>) =
        Single.fromCallable {
            val map = mapOf(
                "official_symptoms" to t.first,
                "customized_symptoms" to t.second,
                "suggested_symptoms" to t.third
            )
            newGsonInstance().toJson(map).toByteArray(Charsets.UTF_8)
        }.subscribeOn(Schedulers.io()).flatMapCompletable { bytes ->
            fileStorageApi.rxCompletable { fileStorageGateway ->
                fileStorageGateway.saveOnFilesDir("cached_symptom.json", bytes)
            }
        }

    fun listSymptom() = fileStorageApi.rxMaybe { fileStorageGateway ->
        val json =
            fileStorageGateway.readOnFilesDir("cached_symptom.json").toString(Charsets.UTF_8)
        val map = newGsonInstance().fromJson<Map<String, List<SymptomData>>>(json)
        Triple(
            map["official_symptoms"] ?: error("missing `official_symptoms`"),
            map["customized_symptoms"] ?: error("missing `customized_symptoms`"),
            map["suggested_symptoms"] ?: error("missing `suggested_symptoms`")
        )
    }.subscribeOn(Schedulers.io())

}