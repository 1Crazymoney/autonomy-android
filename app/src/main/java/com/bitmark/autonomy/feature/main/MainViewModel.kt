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
import io.reactivex.Single
import io.reactivex.functions.BiFunction


class MainViewModel(
    lifecycle: Lifecycle,
    private val assistanceRepo: AssistanceRepository,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer,
    private val serverAuth: ServerAuthentication
) : BaseViewModel(lifecycle) {

    internal val getDataLiveData = CompositeLiveData<Pair<Float, List<HelpRequestModelView>>>()

    internal val getHelpRequestLiveData = CompositeLiveData<HelpRequestModelView>()

    fun getData() {

        val listHelpStream = assistanceRepo.listHelpRequest()
            .map { helpRequests -> helpRequests.map { h -> HelpRequestModelView.newInstance(h) } }

        val getScoreStream = userRepo.getHealthScore()

        getDataLiveData.add(
            rxLiveDataTransformer.single(
                Single.zip(
                    listHelpStream,
                    getScoreStream,
                    BiFunction { helpRequests, score -> Pair(score, helpRequests) })
            )
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