/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote

import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.autonomy.data.source.remote.api.service.AutonomyApi
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class BehaviorRemoteDataSource @Inject constructor(
    autonomyApi: AutonomyApi,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(autonomyApi, rxErrorHandlingComposer) {

    fun listBehavior(lang: String) = autonomyApi.listBehavior(lang).map { res ->
        val officialSymptoms =
            res["official_behaviors"]
                ?: error("invalid response format, do not contains 'official_behaviors' key")
        val neighborhoodSymptoms = res["neighborhood_behaviors"]
            ?: error("invalid response format, do not contains 'neighborhood_behaviors' key")
        Pair(officialSymptoms, neighborhoodSymptoms)
    }.subscribeOn(Schedulers.io())

    fun listAllBehavior(lang: String) =
        autonomyApi.listBehavior(lang, true).map { res ->
            val officialSymptoms =
                res["official_behaviors"]
                    ?: error("invalid response format, do not contains 'official_behaviors' key")
            val customizedSymptoms = res["customized_behaviors"]
                ?: error("invalid response format, do not contains 'customized_behaviors' key")
            Pair(officialSymptoms, customizedSymptoms)
        }.subscribeOn(Schedulers.io())

    fun reportBehaviors(ids: List<String>): Completable {
        val req = mapOf("behaviors" to ids)
        val reqBody =
            newGsonInstance().toJson(req).toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.reportBehavior(reqBody).subscribeOn(Schedulers.io())
    }

    fun addBehavior(name: String, desc: String): Single<String> {
        val req = mapOf("name" to name, "desc" to desc)
        val reqBody =
            newGsonInstance().toJson(req).toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.addBehavior(reqBody)
            .map { res -> res["id"] ?: error("invalid response") }
            .subscribeOn(Schedulers.io())
    }

    fun listBehaviorHistory(beforeSec: Long?, lang: String, limit: Int) =
        autonomyApi.listBehaviorHistory(beforeSec, lang, limit).map { res ->
            res["behaviors_history"] ?: error("invalid response")
        }.subscribeOn(Schedulers.io())

    fun getBehaviorMetric() = autonomyApi.getBehaviorMetric().subscribeOn(Schedulers.io())

}