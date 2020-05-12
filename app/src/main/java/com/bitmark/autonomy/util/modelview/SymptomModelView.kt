/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.data.model.SymptomData
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SymptomModelView(

    val id: String,

    val name: String,

    val desc: String,

    val type: SymptomType? = null

) : ModelView, Parcelable {
    companion object {
        fun newInstance(symptom: SymptomData, type: SymptomType? = null) =
            SymptomModelView(symptom.id, symptom.name, symptom.description, type)
    }
}

enum class SymptomType {
    OFFICIAL, NEIGHBORHOOD
}