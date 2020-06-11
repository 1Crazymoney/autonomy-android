/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote

import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.ResourceData
import com.bitmark.autonomy.data.model.ResourceRatingData
import com.bitmark.autonomy.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.autonomy.data.source.remote.api.service.AutonomyApi
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class ResourceRemoteDataSource @Inject constructor(
    autonomyApi: AutonomyApi,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(autonomyApi, rxErrorHandlingComposer) {

    fun listResourceRating(poiId: String, lang: String) =
        autonomyApi.listResourceRating(poiId, lang).map { res ->
            res["ratings"] ?: error("invalid response")
        }.subscribeOn(Schedulers.io())

    fun updateResourceRatings(ratings: List<ResourceRatingData>): Completable {
        val map =
            mapOf("ratings" to ratings.map { res ->
                mapOf(
                    "resource" to mapOf("id" to res.resource.id),
                    "score" to res.score
                )
            })
        val json = newGsonInstance().toJson(map)
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.updateResourceRatings(reqBody).subscribeOn(Schedulers.io())
    }

    fun listResource(poiId: String, lang: String, important: Boolean = false) =
        autonomyApi.listResource(poiId, lang, important).map { res ->
            res["resources"] ?: error("invalid response")
        }.subscribeOn(Schedulers.io())

    fun addResources(
        poiId: String,
        existingResourceIds: List<String>,
        newResourceNames: List<String>
    ): Single<List<ResourceData>> {
        val map =
            mapOf("resource_ids" to existingResourceIds, "new_resource_names" to newResourceNames)
        val json = newGsonInstance().toJson(map)
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.addResources(poiId, reqBody)
            .map { res -> res["resources"] ?: error("invalid response") }
            .subscribeOn(Schedulers.io())
    }
}