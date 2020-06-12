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
    val resources: List<Resource>?

) : Data

@Parcelize
data class NeighborProfileDetailData(

    @Expose
    @SerializedName("confirm")
    val confirmMetric: ConfirmMetricData,

    @Expose
    @SerializedName("behaviors")
    val behaviorMetric: BehaviorMetricData,

    @Expose
    @SerializedName("symptoms")
    val symptomMetric: SymptomMetricData
) : Data, Parcelable

@Parcelize
data class ConfirmMetricData(

    @Expose
    @SerializedName("score")
    val score: Float

) : Data, Parcelable

@Parcelize
data class BehaviorMetricData(

    @Expose
    @SerializedName("score")
    val score: Float

) : Data, Parcelable

@Parcelize
data class SymptomMetricData(

    @Expose
    @SerializedName("score")
    val score: Float

) : Data, Parcelable

@Parcelize
data class IndividualProfile(
    val score: Float,

    val symptoms: Int,

    val symptomsDelta: Float,

    val behaviors: Int,

    val behaviorsDelta: Float
) : Data, Parcelable

@Parcelize
data class NeighborProfile(

    @Expose
    @SerializedName("score")
    val score: Float,

    @Expose
    @SerializedName("confirm")
    val confirm: Int,

    @Expose
    @SerializedName("confirm_delta")
    val confirmDelta: Float,

    @Expose
    @SerializedName("symptoms")
    val symptoms: Int,

    @Expose
    @SerializedName("symptoms_delta")
    val symptomsDelta: Float,

    @Expose
    @SerializedName("behavior")
    val behaviors: Int,

    @Expose
    @SerializedName("behavior_delta")
    val behaviorsDelta: Float,

    @Expose
    @SerializedName("details")
    val detail: NeighborProfileDetailData?

) : Data, Parcelable


@Parcelize
data class Resource(
    @Expose
    @SerializedName("name")
    val name: String,

    @Expose
    @SerializedName("score")
    val score: Float,

    @Expose
    @SerializedName("ratings")
    val ratings: Int
) : Data, Parcelable
