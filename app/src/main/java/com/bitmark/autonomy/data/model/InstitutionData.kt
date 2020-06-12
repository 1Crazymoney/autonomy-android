/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class InstitutionData(
    @Expose
    @SerializedName("distance")
    val distance: Float,

    @Expose
    @SerializedName("country")
    val country: String,

    @Expose
    @SerializedName("state")
    val state: String,

    @Expose
    @SerializedName("county")
    val county: String,

    @Expose
    @SerializedName("latitude")
    val lat: Double,

    @Expose
    @SerializedName("longitude")
    val lng: Double,

    @Expose
    @SerializedName("name")
    val name: String,

    @Expose
    @SerializedName("address")
    val address: String,

    @Expose
    @SerializedName("phone")
    val phone: String
) : Data