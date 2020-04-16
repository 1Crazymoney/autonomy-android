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
    @SerializedName("id")
    val id: String?,

    @Expose
    @SerializedName("location")
    val location: Location?,

    @Expose
    @SerializedName("address")
    val address: String?,

    @Expose
    @SerializedName("alias")
    val alias: String?,

    @Expose
    @SerializedName("score")
    val score: Int,

    @Expose
    @SerializedName("comfirm")
    val confirmed: Int,

    @Expose
    @SerializedName("comfirm_delta")
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
    @SerializedName("behaviror_delta")
    val behaviorsDelta: Int,

    @Expose
    @SerializedName("guidance")
    val guidance: String
) : Data