/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api.service

import com.bitmark.autonomy.data.model.AccountData
import com.bitmark.autonomy.data.model.AppInfoData
import com.bitmark.autonomy.data.model.JwtData
import com.bitmark.autonomy.data.source.remote.api.request.RegisterAccountRequest
import com.bitmark.autonomy.data.source.remote.api.request.RegisterJwtRequest
import com.bitmark.autonomy.data.source.remote.api.request.RequestHelpRequest
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST


interface AutonomyApi {

    @POST("api/accounts")
    fun registerAccount(@Body request: RegisterAccountRequest): Single<Map<String, AccountData>>

    @POST("api/auth")
    fun registerJwt(@Body request: RegisterJwtRequest): Single<JwtData>

    @GET("api/information")
    fun getAppInfo(): Single<Map<String, AppInfoData>>

    @GET("api/accounts/me")
    fun getAccountInfo(): Single<Map<String, AccountData>>

    @PATCH("api/accounts/me")
    fun updateMetadata(@Body body: RequestBody): Single<Map<String, AccountData>>

    @POST("api/helps")
    fun requestHelp(@Body body: RequestHelpRequest): Completable

}