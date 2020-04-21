/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class AreaProfileData(
    @Expose
    @SerializedName("score")
    val score: Float,

    @Expose
    @SerializedName("confirm")
    val confirmed: Int,

    @Expose
    @SerializedName("confirm_delta")
    val confirmedDelta: Int,

    @Expose
    @SerializedName("symptoms")
    val symptoms: Int,

    @Expose
    @SerializedName("symptoms_delta")
    val symptomsDelta: Int,

    @Expose
    @SerializedName("behavior")
    val behaviors: Int,

    @Expose
    @SerializedName("behavior_delta")
    val behaviorsDelta: Int
) : Data