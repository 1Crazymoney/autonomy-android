/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.requesthelp

import android.os.Parcelable
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
