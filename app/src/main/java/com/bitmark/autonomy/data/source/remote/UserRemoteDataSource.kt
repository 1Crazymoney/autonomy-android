/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote

import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.CoefficientData
import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.data.model.ResourceRatingData
import com.bitmark.autonomy.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.autonomy.data.source.remote.api.request.AddAreaRequest
import com.bitmark.autonomy.data.source.remote.api.request.UpdateProfileFormulaRequest
import com.bitmark.autonomy.data.source.remote.api.service.AutonomyApi
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class UserRemoteDataSource @Inject constructor(
    autonomyApi: AutonomyApi,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(autonomyApi, rxErrorHandlingComposer) {

    fun getHealthScore() = autonomyApi.getHealthScore().map { res ->
        res["score"] ?: error("invalid response")
    }.subscribeOn(Schedulers.io())

    fun addArea(alias: String, address: String, lat: Double, lng: Double) =
        autonomyApi.addArea(AddAreaRequest(alias, address, Location(lat, lng)))
            .subscribeOn(Schedulers.io())

    fun deleteArea(id: String) =
        autonomyApi.deleteArea(id).subscribeOn(Schedulers.io())

    fun listArea() = autonomyApi.listArea().subscribeOn(Schedulers.io())

    fun reorderArea(ids: List<String>): Completable {
        val json = newGsonInstance().toJson(mapOf("order" to ids))
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.reorderArea(reqBody).subscribeOn(Schedulers.io())
    }

    fun rename(id: String, name: String): Completable {
        val json = newGsonInstance().toJson(mapOf("alias" to name))
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.renameArea(id, reqBody).subscribeOn(Schedulers.io())
    }

    fun getYourAutonomyProfile() = autonomyApi.getYourAutonomyProfile().subscribeOn(Schedulers.io())

    fun getAutonomyProfile(id: String) =
        autonomyApi.getAutonomyProfile(id).subscribeOn(Schedulers.io())

    fun listLocationHistory(beforeSec: Long?, limit: Int) =
        autonomyApi.listLocationHistory(beforeSec, limit).map { res ->
            res["locations_history"] ?: error("invalid response")
        }.subscribeOn(Schedulers.io())

    fun getFormula(lang: String) = autonomyApi.getFormula(lang).subscribeOn(Schedulers.io())

    fun deleteFormula() = autonomyApi.deleteFormula().subscribeOn(Schedulers.io())

    fun updateFormula(coefficientData: CoefficientData): Completable {
        val symptomWeights = mutableMapOf<String, Int>()
        coefficientData.symptomWeights.forEach { s -> symptomWeights[s.symptom.id] = s.weight }
        val json = newGsonInstance().toJson(
            mapOf(
                "coefficient" to UpdateProfileFormulaRequest(
                    coefficientData.symptoms,
                    coefficientData.behaviors,
                    coefficientData.confirms,
                    symptomWeights
                )
            )
        )
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.updateFormula(reqBody).subscribeOn(Schedulers.io())
    }

    fun getDebugInfo() = autonomyApi.getDebugInfo().subscribeOn(Schedulers.io())

    fun getDebugInfo(id: String) = autonomyApi.getDebugInfo(id).subscribeOn(Schedulers.io())

    fun updateLocation() = autonomyApi.updateLocation().subscribeOn(Schedulers.io())

    fun listResourceRating(poiId: String) =
        autonomyApi.listResourceRating(poiId).map { res ->
            res["ratings"] ?: error("invalid response")
        }.subscribeOn(Schedulers.io())

    fun updateResourceRatings(ratings: List<ResourceRatingData>): Completable {
        val map =
            mapOf("ratings" to ratings.map { res -> mapOf("resource_id" to res.id, "score" to res.score) })
        val json = newGsonInstance().toJson(map)
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.updateResourceRatings(reqBody).subscribeOn(Schedulers.io())
    }
}