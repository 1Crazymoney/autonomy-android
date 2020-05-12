/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.data.model.BehaviorData
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BehaviorModelView(

    val id: String,

    val name: String,

    val desc: String,

    val type: BehaviorType? = null
) : ModelView, Parcelable {
    companion object {
        fun newInstance(behavior: BehaviorData, type: BehaviorType? = null) =
            BehaviorModelView(behavior.id, behavior.name, behavior.description, type)
    }
}

enum class BehaviorType {
    OFFICIAL, NEIGHBORHOOD
}