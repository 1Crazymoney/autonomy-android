/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.data.model.CoefficientData
import com.bitmark.autonomy.data.model.FormulaData
import com.bitmark.autonomy.data.model.SymptomWeightData
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FormulaModelView(
    val coefficient: CoefficientModelView,

    val isDefault: Boolean
) : ModelView, Parcelable {
    companion object {
        fun newInstance(data: FormulaData) =
            FormulaModelView(CoefficientModelView.newInstance(data.coefficient), data.isDefault)
    }

    fun toCoefficientData() =
        CoefficientData(
            coefficient.symptoms,
            coefficient.behaviors,
            coefficient.confirms,
            coefficient.symptomWeight.toSymptomWeightData()
        )
}

@Parcelize
data class CoefficientModelView(
    var symptoms: Float,

    var behaviors: Float,

    var confirms: Float,

    val symptomWeight: SymptomWeightModelView
) : ModelView, Parcelable {
    companion object {
        fun newInstance(coefficient: CoefficientData) = CoefficientModelView(
            coefficient.symptoms,
            coefficient.behaviors,
            coefficient.confirms,
            SymptomWeightModelView.newInstance(coefficient.symptomWeight)
        )
    }
}

@Parcelize
data class SymptomWeightModelView(

    var face: Int,

    var breath: Int,

    var chest: Int,

    var cough: Int,

    var fatigue: Int,

    var fever: Int,

    var nasal: Int,

    var throat: Int
) : ModelView, Parcelable {
    companion object {
        fun newInstance(symptomWeight: SymptomWeightData) = SymptomWeightModelView(
            symptomWeight.face,
            symptomWeight.breath,
            symptomWeight.chest,
            symptomWeight.cough,
            symptomWeight.fatigue,
            symptomWeight.fever,
            symptomWeight.nasal,
            symptomWeight.throat
        )
    }

    fun toSymptomWeightData() =
        SymptomWeightData(face, breath, chest, cough, fatigue, fever, nasal, throat)
}