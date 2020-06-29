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
data class AutonomyProfileModelView(

    val id: String?,

    val alias: String?,

    val address: String?,

    val rating: Boolean?,

    var owned: Boolean?,

    val location: Location?,

    val hasMoreResources: Boolean?,

    val autonomyScore: Float,

    val autonomyScoreDelta: Float,

    val individualProfile: IndividualProfile?,

    val neighborProfile: NeighborProfile,

    val resources: List<ResourceRatingData>?

) : ModelView, Parcelable {
    companion object {
        fun newInstance(autonomyProfile: AutonomyProfileData) = AutonomyProfileModelView(
            autonomyProfile.id,
            autonomyProfile.alias,
            autonomyProfile.address,
            autonomyProfile.rating,
            autonomyProfile.owned,
            autonomyProfile.location,
            autonomyProfile.hasMoreResource,
            autonomyProfile.autonomyScore,
            autonomyProfile.autonomyScoreDelta,
            autonomyProfile.individualProfile,
            autonomyProfile.neighborProfile,
            autonomyProfile.resources
        )
    }
}