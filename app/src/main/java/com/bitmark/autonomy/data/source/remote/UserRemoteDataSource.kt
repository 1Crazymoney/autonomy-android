/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote

import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.AreaData
import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.autonomy.data.source.remote.api.request.AddAreaRequest
import com.bitmark.autonomy.data.source.remote.api.service.AutonomyApi
import io.reactivex.Completable
import io.reactivex.Single
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

    // TODO update later
    fun deleteArea(id: String) = autonomyApi.deleteArea(id).onErrorComplete().subscribeOn(Schedulers.io())

    fun listArea() = autonomyApi.listArea().map { res ->
        res["points_of_interest"] ?: error("invalid response")
    }.subscribeOn(Schedulers.io()).onErrorResumeNext {
        // TODO remove later
        Single.just(
            listOf(
                AreaData(
                    "1",
                    "Raohe Street Night Market",
                    "",
                    Location(25.053118, 121.577501),
                    17
                ), AreaData(
                    "2",
                    "Tonghua (Linjiang) Street Night Market",
                    "",
                    Location(25.03043, 121.554241),
                    23
                ),
                AreaData(
                    "3",
                    "Nangang Station",
                    "",
                    Location(25.083118, 121.553501),
                    46
                ),
                AreaData(
                    "4",
                    "Taipei City Hall Station",
                    "",
                    Location(25.123118, 121.569501),
                    93
                )
            )
        )
    }

    fun reorderArea(ids: List<String>): Completable {
        val json = newGsonInstance().toJson(mapOf("order" to ids))
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.reorderArea(reqBody).subscribeOn(Schedulers.io())
    }

    // TODO update later
    fun rename(id: String, name: String) = Completable.complete()
}