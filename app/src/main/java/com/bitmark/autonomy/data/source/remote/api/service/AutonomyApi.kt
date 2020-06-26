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
import com.bitmark.autonomy.data.source.remote.api.response.ReportSymptomReponse
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
    fun updateMetadata(@Body body: RequestBody): Completable

    @POST("api/helps")
    fun requestHelp(@Body body: RequestHelpRequest): Completable

    @GET("api/helps")
    fun listHelpRequest(): Single<Map<String, List<HelpRequestData>>>

    @GET("api/helps/{id}")
    fun getHelpRequest(@Path("id") id: String): Single<Map<String, HelpRequestData>>

    @PATCH("api/helps/{id}")
    fun respondHelpRequest(@Path("id") id: String): Completable

    @GET("api/v2/symptoms")
    fun listSymptom(@Query("lang") lang: String, @Query("all") all: Boolean? = null): Single<Map<String, List<SymptomData>>>

    @POST("api/symptoms/report")
    fun reportSymptoms(@Body body: RequestBody): Single<ReportSymptomReponse>

    @POST("api/symptoms")
    fun addSymptom(@Body body: RequestBody): Single<Map<String, String>>

    @GET("api/v2/behaviors")
    fun listBehavior(@Query("lang") lang: String, @Query("all") all: Boolean? = null): Single<Map<String, List<BehaviorData>>>

    @POST("api/behaviors/report")
    fun reportBehavior(@Body body: RequestBody): Completable

    @POST("api/behaviors")
    fun addBehavior(@Body body: RequestBody): Single<Map<String, String>>

    @GET("api/score")
    fun getHealthScore(): Single<Map<String, Float>>

    @POST("api/accounts/me/pois")
    fun addArea(@Body request: AddAreaRequest): Single<AreaData>

    @DELETE("api/accounts/me/pois/{poi_id}")
    fun deleteArea(@Path("poi_id") id: String): Completable

    @GET("api/points-of-interest")
    fun listArea(@Query("resource_id") resourceId: String? = null): Single<List<AreaData>>

    @GET("api/accounts/me/pois")
    fun listMyArea(): Single<List<AreaData>>

    @PATCH("api/accounts/me/pois/{poi_id}")
    fun renameArea(@Path("poi_id") id: String, @Body body: RequestBody): Completable

    @PUT("api/accounts/me/pois")
    fun reorderArea(@Body body: RequestBody): Completable

    @GET("api/autonomy_profile")
    fun getAutonomyProfile(
        @Query("poi_id") poiId: String?,
        @Query("all_resources") allResources: Boolean?,
        @Query("lang") lang: String?,
        @Query("me") me: Boolean?,
        @Query("lat") lat: Double?,
        @Query("lng") lng: Double?
    ): Single<AutonomyProfileData>

    @GET("api/history/symptoms")
    fun listSymptomHistory(@Query("before") before: Long?, @Query("lang") lang: String, @Query("limit") limit: Int): Single<Map<String, List<SymptomHistoryData>>>

    @GET("api/history/behaviors")
    fun listBehaviorHistory(@Query("before") before: Long?, @Query("lang") lang: String, @Query("limit") limit: Int): Single<Map<String, List<BehaviorHistoryData>>>

    @GET("api/history/locations")
    fun listLocationHistory(@Query("before") before: Long?, @Query("limit") limit: Int): Single<Map<String, List<LocationHistoryData>>>

    @GET("api/accounts/me/profile_formula")
    fun getFormula(@Query("lang") lang: String): Single<FormulaData>

    @DELETE("api/accounts/me/profile_formula")
    fun deleteFormula(): Completable

    @PUT("api/accounts/me/profile_formula")
    fun updateFormula(@Body body: RequestBody): Completable

    @GET("api/debug")
    fun getDebugInfo(): Single<DebugInfoData>

    @GET("api/debug/{id}")
    fun getDebugInfo(@Path("id") id: String): Single<DebugInfoData>

    @GET("api/metrics/symptom")
    fun getSymptomMetric(): Single<SymptomMetricData2>

    @GET("api/metrics/behavior")
    fun getBehaviorMetric(): Single<BehaviorMetricData2>

    @HEAD("api/accounts/me")
    fun updateLocation(): Completable

    @POST("api/scores")
    fun listScore(@Body body: RequestBody): Single<Map<String, Array<Float?>>>

    @GET("api/points-of-interest/{poi_id}/resource-ratings")
    fun listResourceRating(@Path("poi_id") poiId: String, @Query("lang") lang: String): Single<Map<String, List<ResourceRatingData>>>

    @PUT("api/points-of-interest/{poi_id}/resource-ratings")
    fun updateResourceRatings(@Path("poi_id") poiId: String, @Body body: RequestBody): Completable

    @GET("api/points-of-interest/{poi_id}/resources")
    fun listResource(@Path("poi_id") poiId: String, @Query("lang") lang: String, @Query("important") important: Boolean): Single<Map<String, List<ResourceData>>>

    @POST("api/points-of-interest/{poi_id}/resources")
    fun addResources(@Path("poi_id") poiId: String, @Body body: RequestBody): Single<Map<String, List<ResourceData>>>

    @GET("api/report-items")
    fun listReportItem(
        @Query("scope") scope: String, @Query("type") type: String,
        @Query("start") start: String, @Query("end") end: String,
        @Query("lang") lang: String, @Query("poi_id") poiId: String?,
        @Query("granularity") granularity: String
    ): Single<Map<String, List<ReportItemData>>>

    @GET("api/resources")
    fun listResource(@Query("suggestion") suggestion: Boolean, @Query("lang") lang: String): Single<Map<String, List<ResourceData>>>

}