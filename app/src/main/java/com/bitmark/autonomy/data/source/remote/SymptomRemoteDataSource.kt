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
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class SymptomRemoteDataSource @Inject constructor(
    autonomyApi: AutonomyApi,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(autonomyApi, rxErrorHandlingComposer) {

    fun listSymptom() = autonomyApi.listSymptom().map { res ->
        res["symptoms"] ?: error("invalid response format")
    }.subscribeOn(Schedulers.io())

    fun reportSymptom(ids: List<String>): Completable {
        val req = mapOf("symptoms" to ids)
        val reqBody =
            newGsonInstance().toJson(req).toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.reportSymptoms(reqBody).subscribeOn(Schedulers.io())
    }
}