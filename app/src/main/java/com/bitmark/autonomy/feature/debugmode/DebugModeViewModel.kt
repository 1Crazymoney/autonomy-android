/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.debugmode

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.model.Location
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.AreaModelView
import com.bitmark.autonomy.util.modelview.DebugInfoModelView


class DebugModeViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getDebugInfoLiveData = CompositeLiveData<DebugInfoModelView>()

    fun getDebugInfo(area: AreaModelView) {
        getDebugInfoLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getDebugInfo(area.id).map { info ->
                    DebugInfoModelView.newInstance(info, area.location, area.id)
                })
        )
    }

    fun getDebugInfo(location: Location) {
        getDebugInfoLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getDebugInfo().map { info ->
                    DebugInfoModelView.newInstance(info, location, null)
                })
        )
    }
}