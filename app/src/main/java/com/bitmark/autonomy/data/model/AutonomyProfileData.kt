/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class AutonomyProfileData(
    @Expose
    @SerializedName("id")
    val id: String?,

    @Expose
    @SerializedName("alias")
    val alias: String?,

    @Expose
    @SerializedName("address")
    val address: String?,

    @Expose
    @SerializedName("rating")
    val rating: Boolean?,

    @Expose
    @SerializedName("owned")
    val owned: Boolean?,

    @Expose
    @SerializedName("location")
    val location: Location?,

    @Expose
    @SerializedName("has_more_resources")
    val hasMoreResource: Boolean?,

    @Expose
    @SerializedName("autonomy_score")
    val autonomyScore: Float,

    @Expose
    @SerializedName("autonomy_score_delta")
    val autonomyScoreDelta: Float,

    @Expose
    @SerializedName("individual")
    val individualProfile: IndividualProfile?,

    @Expose
    @SerializedName("neighbor")
    val neighborProfile: NeighborProfile,

    @Expose
    @SerializedName("resources")
    val resources: List<ResourceRatingData>?

) : Data

@Parcelize
data class IndividualProfile(
    @Expose
    @SerializedName("score")
    val score: Float,

    @Expose
    @SerializedName("score_yesterday")
    val scoreYesterday: Float,

    @Expose
    @SerializedName("symptom")
    val symptoms: Int,

    @Expose
    @SerializedName("symptom_delta")
    val symptomsDelta: Float,

    @Expose
    @SerializedName("behavior")
    val behaviors: Int,

    @Expose
    @SerializedName("behavior_delta")
    val behaviorsDelta: Float
) : Data, Parcelable

@Parcelize
data class NeighborProfile(

    @Expose
    @SerializedName("score")
    val score: Float,

    @Expose
    @SerializedName("score_delta")
    val scoreDelta: Float,

    @Expose
    @SerializedName("confirm")
    val confirm: Int,

    @Expose
    @SerializedName("confirm_delta")
    val confirmDelta: Float,

    @Expose
    @SerializedName("symptom")
    val symptoms: Int,

    @Expose
    @SerializedName("symptom_delta")
    val symptomsDelta: Float,

    @Expose
    @SerializedName("behavior")
    val behaviors: Int,

    @Expose
    @SerializedName("behavior_delta")
    val behaviorsDelta: Float

) : Data, Parcelable