/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.location

import android.os.Parcelable
import com.bitmark.autonomy.data.model.Location
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaceAutoComplete(
    val placeId: String? = null,
    val poiId: String? = null,
    val primaryText: String? = null,
    val secondaryText: String? = null,
    val desc: String? = null,
    val alias: String? = null,
    val address: String? = null,
    val distance: Float? = null,
    val resourceScore: Float? = null,
    var location: Location? = null
) : Parcelable