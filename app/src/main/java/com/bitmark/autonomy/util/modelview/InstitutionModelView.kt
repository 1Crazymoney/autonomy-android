/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.data.model.InstitutionData
import kotlinx.android.parcel.Parcelize

@Parcelize
class InstitutionModelView(
    val distance: Float,

    val country: String,

    val state: String,

    val county: String,

    val lat: Double,

    val lng: Double,

    val name: String,

    val address: String,

    val phone: String
) : ModelView, Parcelable {

    companion object {
        fun newInstance(institution: InstitutionData) = InstitutionModelView(
            institution.distance,
            institution.country,
            institution.state,
            institution.county,
            institution.lat,
            institution.lng,
            institution.name,
            institution.address,
            institution.phone
        )
    }
}