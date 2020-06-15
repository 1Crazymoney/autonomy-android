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
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class AppRemoteDataSource @Inject constructor(
    autonomyApi: AutonomyApi,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(autonomyApi, rxErrorHandlingComposer) {

    fun getAppInfo() = autonomyApi.getAppInfo().map { res ->
        res["information"] ?: error("invalid response")
    }.subscribeOn(Schedulers.io())

    fun listScore(addresses: List<String>): Single<Array<Float?>> {
        val addressArray = addresses.map { a -> mapOf("address" to a) }
        val reqMap = mapOf("places" to addressArray)
        val reqBody =
            newGsonInstance().toJson(reqMap).toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.listScore(reqBody)
            .map { res -> res["results"] ?: error("invalid response") }.subscribeOn(Schedulers.io())
    }

    fun listReportItem(
        scope: String,
        type: String,
        start: Long,
        end: Long,
        lang: String,
        poiId: String?
    ) = autonomyApi.listReportItem(
        scope,
        type,
        start,
        end,
        lang,
        poiId
    ).map { res -> res["report_items"] ?: error("invalid response") }.subscribeOn(Schedulers.io())
}