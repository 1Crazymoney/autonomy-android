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


class HelpRequestData(
    @Expose
    @SerializedName("id")
    val id: String,

    @Expose
    @SerializedName("requester")
    val requester: String,

    @Expose
    @SerializedName("helper")
    val helper: String,

    @Expose
    @SerializedName("subject")
    val subject: String,

    @Expose
    @SerializedName("exact_needs")
    val exactNeeds: String,

    @Expose
    @SerializedName("meeting_location")
    val meetingLocation: String,

    @Expose
    @SerializedName("contact_info")
    val contactInfo: String,

    @Expose
    @SerializedName("state")
    val state: HelpState,

    @Expose
    @SerializedName("created_at")
    val createdAt: String

) : Data

@Parcelize
enum class HelpState : Parcelable {
    @Expose
    @SerializedName("PENDING")
    PENDING,

    @Expose
    @SerializedName("RESPONDED")
    RESPONDED
}