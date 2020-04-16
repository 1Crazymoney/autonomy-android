/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api.service

import com.bitmark.autonomy.data.model.*
import com.bitmark.autonomy.data.source.remote.api.request.AddAreaRequest
import com.bitmark.autonomy.data.source.remote.api.request.RegisterAccountRequest
import com.bitmark.autonomy.data.source.remote.api.request.RegisterJwtRequest
import com.bitmark.autonomy.data.source.remote.api.request.RequestHelpRequest
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*


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

    @GET("api/helps")
    fun listHelpRequest(): Single<Map<String, List<HelpRequestData>>>

    @GET("api/helps/{id}")
    fun getHelpRequest(@Path("id") id: String): Single<Map<String, HelpRequestData>>

    @PATCH("api/helps/{id}")
    fun respondHelpRequest(@Path("id") id: String): Completable

    @GET("api/symptoms")
    fun listSymptom(): Single<Map<String, List<SymptomData>>>

    @POST("api/report")
    fun reportSymptoms(@Body body: RequestBody): Completable

    @GET("api/behaviors")
    fun listBehavior(): Single<Map<String, List<BehaviorData>>>

    @POST("api/report")
    fun reportBehavior(@Body body: RequestBody): Completable

    @GET("api/score")
    fun getHealthScore(): Single<Map<String, Float>>

    @POST("api/points-of-interest")
    fun addArea(@Body request: AddAreaRequest): Single<AreaData>

    @DELETE("api/points-of-interest/{id}")
    fun deleteArea(@Path("id") id: String): Completable

    @GET("api/points-of-interest")
    fun listArea(): Single<Map<String, List<AreaData>>>

    @PUT("api/points-of-interest/order")
    fun reorderArea(@Body body: RequestBody): Completable

    @GET("api/area_profile")
    fun getCurrentAreaProfile(): Single<AreaProfileData>

    @GET("api/area_profile/{id}")
    fun getAreaProfile(@Path("id") id: String): Single<AreaProfileData>

    @PATCH("api/points-of-interest/{id}")
    fun renameArea(@Path("id") id: String, @Body body: RequestBody): Completable

}