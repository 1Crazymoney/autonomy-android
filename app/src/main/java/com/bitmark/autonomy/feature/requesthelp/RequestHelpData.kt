/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.requesthelp

import android.os.Parcelable
import com.bitmark.autonomy.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RequestHelpData(
    val type: Type,

    var exactNeed: String = "",

    var meetingLocation: String = "",

    var contactInfo: String = ""
) : Parcelable

@Parcelize
enum class Type : Parcelable {
    FOOD, MEDICINE, MEDICAL_CARE, SAFE_LOCATION;

    companion object
}

val Type.value: String
    get() = when (this) {
        Type.FOOD -> "food"
        Type.MEDICINE -> "medicine"
        Type.MEDICAL_CARE -> "medical_care"
        Type.SAFE_LOCATION -> "safe_location"
    }

fun Type.Companion.fromString(type: String) = when (type) {
    "food" -> Type.FOOD
    "medicine" -> Type.MEDICINE
    "medical_care" -> Type.MEDICAL_CARE
    "safe_location" -> Type.SAFE_LOCATION
    else -> error("invalid type: $type")
}

val Type.resId: Int
    get() = when (this) {
        Type.FOOD -> R.string.need_access_food
        Type.MEDICINE -> R.string.need_access_medicine
        Type.MEDICAL_CARE -> R.string.need_transport_healthcare
        Type.SAFE_LOCATION -> R.string.need_traveling_to_safe_location
    }
