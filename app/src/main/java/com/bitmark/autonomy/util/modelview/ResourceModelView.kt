/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import android.os.Parcelable
import com.bitmark.autonomy.data.model.ResourceData
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ResourceModelView(val id: String?, val name: String) : ModelView, Parcelable {

    companion object {
        fun newInstance(resource: ResourceData) = ResourceModelView(resource.id, resource.name)
    }
}

fun ResourceModelView.toResourceData() = ResourceData(id!!, name)