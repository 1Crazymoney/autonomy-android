/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FormulaData(
    @Expose
    @SerializedName("coefficient")
    val coefficient: CoefficientData,

    @Expose
    @SerializedName("is_default")
    val isDefault: Boolean
) : Data

data class CoefficientData(
    @Expose
    @SerializedName("symptoms")
    val symptoms: Float,

    @Expose
    @SerializedName("behaviors")
    val behaviors: Float,

    @Expose
    @SerializedName("confirms")
    val confirms: Float,

    @Expose
    @SerializedName("symptom_weights")
    val symptomWeight: SymptomWeightData
) : Data

data class SymptomWeightData(

    @Expose
    @SerializedName("face")
    val face: Int,

    @Expose
    @SerializedName("breath")
    val breath: Int,

    @Expose
    @SerializedName("chest")
    val chest: Int,

    @Expose
    @SerializedName("cough")
    val cough: Int,

    @Expose
    @SerializedName("fatigue")
    val fatigue: Int,

    @Expose
    @SerializedName("fever")
    val fever: Int,

    @Expose
    @SerializedName("nasal")
    val nasal: Int,

    @Expose
    @SerializedName("throat")
    val throat: Int
) : Data