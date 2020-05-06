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

data class AreaProfileData(
    @Expose
    @SerializedName("score")
    val score: Float,

    @Expose
    @SerializedName("confirm")
    val confirmed: Int,

    @Expose
    @SerializedName("confirm_delta")
    val confirmedDelta: Float,

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
    val detail: AreaProfileDetailData?
) : Data

@Parcelize
data class AreaProfileDetailData(

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
    @SerializedName("yesterday")
    val yesterday: Int,

    @Expose
    @SerializedName("today")
    val today: Int,

    @Expose
    @SerializedName("score")
    val score: Float
) : Data, Parcelable

@Parcelize
data class BehaviorMetricData(
    @Expose
    @SerializedName("behavior_total")
    val totalBehaviors: Int,

    @Expose
    @SerializedName("total_people")
    val totalPeople: Int,

    @Expose
    @SerializedName("max_score_per_person")
    val maxScorePerPerson: Int,

    @Expose
    @SerializedName("behavior_customized_total")
    val totalCustomized: Int,

    @Expose
    @SerializedName("score")
    val score: Float

) : Data, Parcelable

@Parcelize
data class SymptomMetricData(

    @Expose
    @SerializedName("total_weight")
    val totalWeight: Int,

    @Expose
    @SerializedName("total_people")
    val totalPeople: Int,

    @Expose
    @SerializedName("custom_symptom_count")
    val customSymptomCount: Int,

    @Expose
    @SerializedName("customized_weight")
    val customizedWeight: Int,

    @Expose
    @SerializedName("max_weight")
    val maxWeight: Int,

    @Expose
    @SerializedName("score")
    val score: Float

) : Data, Parcelable