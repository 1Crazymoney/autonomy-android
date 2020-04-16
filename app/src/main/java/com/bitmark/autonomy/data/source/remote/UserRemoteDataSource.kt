/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote

import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.AreaData
import com.bitmark.autonomy.data.model.AreaProfileData
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
            .onErrorResumeNext {
                // TODO remove later
                Single.just(AreaData("test_id", alias, address, Location(lat, lng), 39))
            }
            .subscribeOn(Schedulers.io())

    // TODO update later
    fun deleteArea(id: String) =
        autonomyApi.deleteArea(id).onErrorComplete().subscribeOn(Schedulers.io())

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

    fun rename(id: String, name: String): Completable {
        val json = newGsonInstance().toJson(mapOf("alias" to name))
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.renameArea(id, reqBody).subscribeOn(Schedulers.io())
    }

    fun getCurrentAreaProfile() = autonomyApi.getCurrentAreaProfile().onErrorResumeNext {
        // TODO remove later
        Single.just(
            AreaProfileData(
                null,
                null,
                null,
                null,
                57,
                2,
                -1,
                1321,
                -54,
                3431,
                31,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco."
            )
        )
    }.subscribeOn(Schedulers.io())

    fun getAreaProfile(id: String) = autonomyApi.getAreaProfile(id).onErrorResumeNext {
        Single.just(
            when (id) {
                "1" -> AreaProfileData(
                    null,
                    null,
                    null,
                    null,
                    17,
                    0,
                    -2,
                    425,
                    58,
                    1242,
                    174,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco."
                )
                "2" -> AreaProfileData(
                    null,
                    null,
                    null,
                    null,
                    23,
                    12,
                    9,
                    3425,
                    534,
                    7535,
                    -253,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco."
                )
                "3" -> AreaProfileData(
                    null,
                    null,
                    null,
                    null,
                    46,
                    7,
                    -3,
                    425,
                    -12,
                    674,
                    23,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco."
                )
                "4" -> AreaProfileData(
                    null,
                    null,
                    null,
                    null,
                    93,
                    34,
                    32,
                    5325,
                    -324,
                    5463,
                    -24,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco."
                )
                else -> error("invalid test data")
            }
        )
    }.subscribeOn(Schedulers.io())
}