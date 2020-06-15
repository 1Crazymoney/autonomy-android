/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class ReportItemData(
    @Expose
    @SerializedName("name")
    val name: String,

    @Expose
    @SerializedName("value")
    val value: Float?,

    @Expose
    @SerializedName("change_rate")
    val changeRate: Float?
) : Data

enum class ReportType(val value: String) {
    SCORE("score"), SYMPTOM("symptom"), BEHAVIOR("behavior"), CASE("case")
}

enum class ReportScope(val value: String) {
    INDIVIDUAL("individual"), NEIGHBORHOOD("neighborhood"), POI("poi")
}