/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.history

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.SymptomRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.SymptomHistoryModelView


class SymptomHistoryViewModel(
    lifecycle: Lifecycle,
    private val symptomRepo: SymptomRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val nextSymptomHistoryLiveData = CompositeLiveData<List<SymptomHistoryModelView>>()

    internal val refreshSymptomHistoryLiveData = CompositeLiveData<List<SymptomHistoryModelView>>()

    private var lastTimestamp = -1L

    fun nextSymptomHistory(lang: String) {
        nextSymptomHistoryLiveData.add(rxLiveDataTransformer.single(symptomHistoryStream(lang)))
    }

    fun refreshSymptomHistory(lang: String) {
        lastTimestamp = -1L
        refreshSymptomHistoryLiveData.add(rxLiveDataTransformer.single(symptomHistoryStream(lang)))
    }

    private fun symptomHistoryStream(lang: String) = if (lastTimestamp == -1L) {
        symptomRepo.listSymptomHistory(lang = lang)
    } else {
        symptomRepo.listSymptomHistory(beforeSec = lastTimestamp, lang = lang)
    }.map { symptomHistories ->
        if (symptomHistories.isNotEmpty()) {
            lastTimestamp = symptomHistories.minBy { s -> s.timestamp }!!.timestamp
            symptomHistories.map { s -> SymptomHistoryModelView.newInstance(s) }
        } else {
            listOf()
        }
    }
}