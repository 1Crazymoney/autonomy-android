/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.data.model.AreaProfileData
import com.bitmark.autonomy.data.model.AreaProfileDetailData
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

    val detail: AreaProfileDetailData

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
            areaProfile.detail
        )
    }
}