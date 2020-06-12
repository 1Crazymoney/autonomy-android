/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote

import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.autonomy.data.source.remote.api.response.ReportSymptomReponse
import com.bitmark.autonomy.data.source.remote.api.service.AutonomyApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class SymptomRemoteDataSource @Inject constructor(
    autonomyApi: AutonomyApi,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(autonomyApi, rxErrorHandlingComposer) {

    fun listAllSymptom(lang: String) =
        autonomyApi.listSymptom(lang, true).map { res ->
            val officialSymptoms =
                res["official_symptoms"]
                    ?: error("invalid response format, do not contains 'official_symptoms' key")
            val customizedSymptoms = res["customized_symptoms"]
                ?: error("invalid response format, do not contains 'customized_symptoms' key")
            val suggestedSymptoms = res["suggested_symptoms"]
                ?: error("invalid response format, do not contains 'suggested_symptoms' key")
            Triple(officialSymptoms, customizedSymptoms, suggestedSymptoms)
        }.subscribeOn(Schedulers.io())

    fun listSymptom(lang: String) = autonomyApi.listSymptom(lang).map { res ->
        val officialSymptoms =
            res["official_symptoms"]
                ?: error("invalid response format, do not contains 'official_symptoms' key")
        val neighborhoodSymptoms = res["neighborhood_symptoms"]
            ?: error("invalid response format, do not contains 'neighborhood_symptoms' key")
        Pair(officialSymptoms, neighborhoodSymptoms)
    }.subscribeOn(Schedulers.io())

    fun reportSymptom(ids: List<String>): Single<ReportSymptomReponse> {
        val req = mapOf("symptoms" to ids)
        val reqBody =
            newGsonInstance().toJson(req).toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.reportSymptoms(reqBody).subscribeOn(Schedulers.io())
    }

    fun addSymptom(name: String, desc: String): Single<String> {
        val req = mapOf("name" to name, "desc" to desc)
        val reqBody =
            newGsonInstance().toJson(req).toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.addSymptom(reqBody).map { res -> res["id"] ?: error("invalid response") }
            .subscribeOn(Schedulers.io())
    }

    fun listSymptomHistory(beforeSec: Long?, lang: String, limit: Int) =
        autonomyApi.listSymptomHistory(beforeSec, lang, limit).map { res ->
            res["symptoms_history"] ?: error("invalid response")
        }.subscribeOn(Schedulers.io())

    fun getSymptomMetric() = autonomyApi.getSymptomMetric().subscribeOn(Schedulers.io())
}