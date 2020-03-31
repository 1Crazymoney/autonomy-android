/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.AssistanceRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.HelpRequestModelView


class MainViewModel(
    lifecycle: Lifecycle,
    private val assistanceRepo: AssistanceRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listHelpRequestLiveData = CompositeLiveData<List<HelpRequestModelView>>()

    fun listHelpRequest() {
        listHelpRequestLiveData.add(
            rxLiveDataTransformer.single(assistanceRepo.listHelpRequest()
                .map { helpRequests ->
                    helpRequests.map { h -> HelpRequestModelView.newInstance(h) }
                })
        )
    }
}