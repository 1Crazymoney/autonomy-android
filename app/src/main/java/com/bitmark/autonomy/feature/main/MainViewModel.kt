/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.AssistanceRepository
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.auth.ServerAuthentication
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.HelpRequestModelView


class MainViewModel(
    lifecycle: Lifecycle,
    private val assistanceRepo: AssistanceRepository,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val serverAuth: ServerAuthentication
) : BaseViewModel(lifecycle) {

    internal val getHealthScoreLiveData = CompositeLiveData<Float>()

    internal val listHelpRequestLiveData = CompositeLiveData<List<HelpRequestModelView>>()

    internal val getHelpRequestLiveData = CompositeLiveData<HelpRequestModelView>()

    fun getHealthScore() {
        getHealthScoreLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getHealthScore()
            )
        )
    }

    fun listHelpRequest() {
        listHelpRequestLiveData.add(
            rxLiveDataTransformer.single(assistanceRepo.listHelpRequest()
                .map { helpRequests ->
                    helpRequests.sortedByDescending { h -> h.createdAt }
                        .map { h ->
                            HelpRequestModelView.newInstance(h)
                        }
                })
        )
    }

    fun getHelpRequest(id: String) {
        getHelpRequestLiveData.add(
            rxLiveDataTransformer.single(assistanceRepo.getHelpRequest(id)
                .map { h -> HelpRequestModelView.newInstance(h) })
        )
    }

    fun startServerAuth() {
        serverAuth.start()
    }

    fun stopServerAuth() {
        serverAuth.stop()
    }
}