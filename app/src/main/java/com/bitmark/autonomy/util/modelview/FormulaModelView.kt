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
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FormulaModelView(
    val coefficient: CoefficientData,

    val isDefault: Boolean
) : ModelView, Parcelable {
    companion object {
        fun newInstance(data: FormulaData) =
            FormulaModelView(data.coefficient, data.isDefault)
    }

    fun toCoefficientData() =
        CoefficientData(
            coefficient.symptoms,
            coefficient.behaviors,
            coefficient.confirms,
            coefficient.symptomWeights
        )
}