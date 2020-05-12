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
import com.bitmark.autonomy.util.ext.append
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.SymptomModelView
import com.bitmark.autonomy.util.modelview.SymptomType

class SymptomReportViewModel(
    lifecycle: Lifecycle,
    private val symptomRepo: SymptomRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listSymptomLiveData = CompositeLiveData<List<SymptomModelView>>()

    internal val reportSymptomLiveData = CompositeLiveData<Any>()

    fun listSymptom(lang: String) {
        listSymptomLiveData.add(rxLiveDataTransformer.single(symptomRepo.listSymptom(lang).map { p ->
            val officialSymptoms =
                p.first.map { s -> SymptomModelView.newInstance(s, SymptomType.OFFICIAL) }
            val neighborhoodSymptoms =
                p.second.map { s -> SymptomModelView.newInstance(s, SymptomType.NEIGHBORHOOD) }
            officialSymptoms.toMutableList().append(neighborhoodSymptoms)
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