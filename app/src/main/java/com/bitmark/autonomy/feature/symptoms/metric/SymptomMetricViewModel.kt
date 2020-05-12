/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.metric

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.SymptomRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.SymptomMetricModelView


class SymptomMetricViewModel(
    lifecycle: Lifecycle,
    private val symptomRepo: SymptomRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getSymptomMetricLiveData = CompositeLiveData<SymptomMetricModelView>()

    fun getMetric() {
        getSymptomMetricLiveData.add(rxLiveDataTransformer.single(symptomRepo.getSymptomMetric().map { m ->
            SymptomMetricModelView.newInstance(
                m
            )
        }))
    }

}