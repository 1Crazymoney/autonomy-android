/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import com.bitmark.autonomy.data.model.SymptomData


data class SymptomModelView(

    val id: String,

    val symptom: String,

    val symptomDes: String
) : ModelView {
    companion object {
        fun newInstance(symptom: SymptomData) =
            SymptomModelView(symptom.id, symptom.name, symptom.description)
    }
}