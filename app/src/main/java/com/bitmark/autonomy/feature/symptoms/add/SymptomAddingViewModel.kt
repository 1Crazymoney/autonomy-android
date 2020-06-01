/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.add

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.SymptomRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.SymptomModelView


class SymptomAddingViewModel(
    lifecycle: Lifecycle,
    private val symptomRepo: SymptomRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val addSymptomLiveData = CompositeLiveData<SymptomModelView>()

    fun addSymptom(newSymptomData: NewSymptomData) {
        addSymptomLiveData.add(
            rxLiveDataTransformer.single(
                symptomRepo.addSymptom(
                    newSymptomData.title,
                    newSymptomData.description
                ).map { id ->
                    SymptomModelView(id, newSymptomData.title)
                })
        )
    }

}