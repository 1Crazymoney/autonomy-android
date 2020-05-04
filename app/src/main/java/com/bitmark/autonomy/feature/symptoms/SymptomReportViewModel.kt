/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.SymptomRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.SymptomModelView

class SymptomReportViewModel(
    lifecycle: Lifecycle,
    private val symptomRepo: SymptomRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listSymptomLiveData = CompositeLiveData<List<SymptomModelView>>()

    internal val reportSymptomLiveData = CompositeLiveData<Any>()

    fun listSymptom(lang: String) {
        listSymptomLiveData.add(rxLiveDataTransformer.single(symptomRepo.listSymptom(lang).map { symptoms ->
            symptoms.map { s ->
                SymptomModelView.newInstance(s)
            }
        }))
    }

    fun reportSymptoms(symptomIds: List<String>) {
        reportSymptomLiveData.add(
            rxLiveDataTransformer.completable(
                symptomRepo.reportSymptom(
                    symptomIds
                )
            )
        )
    }

}