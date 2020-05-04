/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class UpdateProfileFormulaRequest(
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
    val symptomWeight: Map<String, Int>
) : Request