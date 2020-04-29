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

    fun nextBehaviorHistory() {
        nextBehaviorHistoryLiveData.add(rxLiveDataTransformer.single(behaviorHistoryStream()))
    }

    fun refreshBehaviorHistory() {
        lastTimestamp = -1L
        refreshBehaviorHistoryLiveData.add(rxLiveDataTransformer.single(behaviorHistoryStream()))
    }

    private fun behaviorHistoryStream() = if (lastTimestamp == -1L) {
        behaviorRepo.listBehaviorHistory(System.currentTimeMillis() / 1000)
    } else {
        behaviorRepo.listBehaviorHistory(lastTimestamp)
    }.map { behaviorHistories ->
        lastTimestamp = behaviorHistories.minBy { s -> s.timestamp }!!.timestamp
        behaviorHistories.map { b -> BehaviorHistoryModelView.newInstance(b) }
    }
}