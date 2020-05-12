/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.metric

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.BehaviorRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.BehaviorMetricModelView


class BehaviorMetricViewModel(
    lifecycle: Lifecycle,
    private val behaviorRepo: BehaviorRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getBehaviorMetricLiveData = CompositeLiveData<BehaviorMetricModelView>()

    fun getMetric() {
        getBehaviorMetricLiveData.add(
            rxLiveDataTransformer.single(
                behaviorRepo.getBehaviorMetric().map { m ->
                    BehaviorMetricModelView.newInstance(m)
                })
        )
    }

}