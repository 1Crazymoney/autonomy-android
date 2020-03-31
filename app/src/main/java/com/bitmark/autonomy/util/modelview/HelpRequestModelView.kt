/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.data.model.HelpRequestData
import com.bitmark.autonomy.data.model.HelpState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HelpRequestModelView(
    val id: String,

    val subject: String,

    val exactNeeds: String,

    val meetingLocation: String,

    val contactInfo: String,

    val state: HelpState,

    val createdAt: String
) : ModelView, Parcelable {
    companion object {
        fun newInstance(helpData: HelpRequestData) = HelpRequestModelView(
            helpData.id,
            helpData.subject,
            helpData.exactNeeds,
            helpData.meetingLocation,
            helpData.contactInfo,
            helpData.state,
            helpData.createdAt
        )
    }
}

fun HelpRequestModelView.isResponded() = state == HelpState.RESPONDED