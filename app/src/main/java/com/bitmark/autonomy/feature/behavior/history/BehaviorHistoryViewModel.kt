/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.history

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.BehaviorRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.BehaviorHistoryModelView


class BehaviorHistoryViewModel(
    lifecycle: Lifecycle,
    private val behaviorRepo: BehaviorRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val nextBehaviorHistoryLiveData = CompositeLiveData<List<BehaviorHistoryModelView>>()

    internal val refreshBehaviorHistoryLiveData =
        CompositeLiveData<List<BehaviorHistoryModelView>>()

    private var lastTimestamp = -1L

    fun nextBehaviorHistory(lang: String) {
        nextBehaviorHistoryLiveData.add(rxLiveDataTransformer.single(behaviorHistoryStream(lang)))
    }

    fun refreshBehaviorHistory(lang: String) {
        lastTimestamp = -1L
        refreshBehaviorHistoryLiveData.add(rxLiveDataTransformer.single(behaviorHistoryStream(lang)))
    }

    private fun behaviorHistoryStream(lang: String) = if (lastTimestamp == -1L) {
        behaviorRepo.listBehaviorHistory(lang = lang)
    } else {
        behaviorRepo.listBehaviorHistory(beforeSec = lastTimestamp, lang = lang)
    }.map { behaviorHistories ->
        if (behaviorHistories.isNotEmpty()) {
            lastTimestamp = behaviorHistories.minBy { s -> s.timestamp }!!.timestamp
            behaviorHistories.map { b -> BehaviorHistoryModelView.newInstance(b) }
        } else {
            listOf()
        }
    }
}