/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.data.model.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AreaProfileModelView(
    val score: Float,

    val confirmed: Int,

    val confirmedDelta: Float,

    val symptoms: Int,

    val symptomsDelta: Float,

    val behaviors: Int,

    val behaviorsDelta: Float,

    val detail: AreaProfileDetailModelView

) : ModelView, Parcelable {
    companion object {
        fun newInstance(areaProfile: AreaProfileData) = AreaProfileModelView(
            areaProfile.score,
            areaProfile.confirmed,
            areaProfile.confirmedDelta,
            areaProfile.symptoms,
            areaProfile.symptomsDelta,
            areaProfile.behaviors,
            areaProfile.behaviorsDelta,
            AreaProfileDetailModelView.newInstance(areaProfile.detail)
        )
    }
}

@Parcelize
data class AreaProfileDetailModelView(

    val confirmMetric: ConfirmMetricModelView,

    val behaviorMetric: BehaviorMetricModelView,

    val symptomMetric: SymptomMetricModelView
) : ModelView, Parcelable {
    companion object {
        fun newInstance(areaProfileDetail: AreaProfileDetailData) = AreaProfileDetailModelView(
            ConfirmMetricModelView.newInstance(areaProfileDetail.confirmMetric),
            BehaviorMetricModelView.newInstance(areaProfileDetail.behaviorMetric),
            SymptomMetricModelView.newInstance(areaProfileDetail.symptomMetric)
        )
    }
}

@Parcelize
data class ConfirmMetricModelView(
    val yesterday: Int,

    val today: Int,

    val score: Float
) : ModelView, Parcelable {
    companion object {
        fun newInstance(confirmMetric: ConfirmMetricData) = ConfirmMetricModelView(
            confirmMetric.yesterday,
            confirmMetric.today,
            confirmMetric.score
        )
    }
}

@Parcelize
data class BehaviorMetricModelView(
    val totalBehaviors: Int,

    val totalPeople: Int,

    val maxScorePerPerson: Int,

    val score: Float

) : ModelView, Parcelable {
    companion object {
        fun newInstance(behaviorMetric: BehaviorMetricData) = BehaviorMetricModelView(
            behaviorMetric.totalBehaviors,
            behaviorMetric.totalPeople,
            behaviorMetric.maxScorePerPerson,
            behaviorMetric.score
        )
    }
}

@Parcelize
data class SymptomMetricModelView(

    val totalSymptom: Int,

    val totalPeople: Int,

    val maxScorePerPerson: Int,

    val score: Float

) : ModelView, Parcelable {
    companion object {
        fun newInstance(symptomMetric: SymptomMetricData) = SymptomMetricModelView(
            symptomMetric.totalSymptom,
            symptomMetric.totalPeople,
            symptomMetric.maxScorePerPerson,
            symptomMetric.score
        )
    }
}