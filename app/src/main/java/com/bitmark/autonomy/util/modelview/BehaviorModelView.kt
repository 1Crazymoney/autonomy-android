/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.BehaviorData

data class BehaviorModelView(

    val id: String,

    val behavior: String,

    val behaviorDes: String
) : ModelView {
    companion object {
        fun newInstance(behavior: BehaviorData) =
            BehaviorModelView(behavior.id, behavior.name, behavior.description)
    }
}