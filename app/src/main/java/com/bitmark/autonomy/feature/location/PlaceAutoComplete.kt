/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.location

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlaceAutoComplete(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val desc: String
) : Parcelable