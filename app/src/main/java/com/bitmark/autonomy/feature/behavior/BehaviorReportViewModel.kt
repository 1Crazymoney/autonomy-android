/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.BehaviorRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.BehaviorModelView


class BehaviorReportViewModel(
    lifecycle: Lifecycle,
    private val behaviorRepo: BehaviorRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listBehaviorLiveData = CompositeLiveData<List<BehaviorModelView>>()

    internal val reportBehaviorsLiveData = CompositeLiveData<Any>()

    fun listBehavior() {
        listBehaviorLiveData.add(rxLiveDataTransformer.single(behaviorRepo.listBehavior().map { symptoms ->
            symptoms.map { s ->
                BehaviorModelView.newInstance(s)
            }
        }))
    }

    fun reportBehaviors(behaviorIds: List<String>) {
        reportBehaviorsLiveData.add(
            rxLiveDataTransformer.completable(
                behaviorRepo.reportBehaviors(
                    behaviorIds
                )
            )
        )
    }

}