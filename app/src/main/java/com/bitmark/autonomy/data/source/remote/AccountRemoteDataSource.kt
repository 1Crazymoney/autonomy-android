/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote

import com.bitmark.autonomy.data.ext.newGsonInstance
import com.bitmark.autonomy.data.model.AccountData
import com.bitmark.autonomy.data.source.local.Jwt
import com.bitmark.autonomy.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.autonomy.data.source.remote.api.request.RegisterAccountRequest
import com.bitmark.autonomy.data.source.remote.api.request.RegisterJwtRequest
import com.bitmark.autonomy.data.source.remote.api.service.AutonomyApi
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


class AccountRemoteDataSource @Inject constructor(
    autonomyApi: AutonomyApi,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(autonomyApi, rxErrorHandlingComposer) {

    fun registerServerJwt(
        timestamp: String,
        signature: String,
        requester: String
    ): Completable {
        return autonomyApi.registerJwt(
            RegisterJwtRequest(
                timestamp,
                signature,
                requester
            )
        ).map { jwt ->
            val jwtCache = Jwt.getInstance()
            jwtCache.token = jwt.token
            jwtCache.expiredAt = System.currentTimeMillis() + jwt.expiredIn * 1000
        }.ignoreElement().subscribeOn(Schedulers.io())
    }

    fun registerServerAccount(
        encPubKey: String,
        metadata: Map<String, String>
    ): Single<AccountData> {
        return autonomyApi.registerAccount(RegisterAccountRequest(encPubKey, metadata))
            .map { res -> res["result"] ?: error("invalid response") }
            .subscribeOn(Schedulers.io())
    }

    fun getAccountInfo() =
        autonomyApi.getAccountInfo().map { res ->
            res["result"] ?: error("invalid response")
        }

    fun updateMetadata(metadata: Map<String, String>): Single<AccountData> {
        val json = newGsonInstance().toJson(mapOf("metadata" to metadata))
        val reqBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        return autonomyApi.updateMetadata(reqBody)
            .map { res -> res["result"] ?: error("invalid response") }.subscribeOn(Schedulers.io())
    }

}