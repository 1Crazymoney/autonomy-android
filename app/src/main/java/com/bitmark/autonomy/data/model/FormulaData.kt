/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class FormulaData(
    @Expose
    @SerializedName("coefficient")
    val coefficient: CoefficientData,

    @Expose
    @SerializedName("is_default")
    val isDefault: Boolean
) : Data

@Parcelize
data class CoefficientData(
    @Expose
    @SerializedName("symptoms")
    var symptoms: Float,

    @Expose
    @SerializedName("behaviors")
    var behaviors: Float,

    @Expose
    @SerializedName("confirms")
    var confirms: Float,

    @Expose
    @SerializedName("symptom_weights")
    val symptomWeights: List<SymptomWeightData>
) : Data, Parcelable

@Parcelize
data class SymptomWeightData(

    @Expose
    @SerializedName("symptom")
    val symptom: SymptomData,

    @Expose
    @SerializedName("weight")
    var weight: Int
) : Data, Parcelable