/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.locationhistory

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.LocationHistoryModelView


class LocationHistoryViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val nextLocationHistoryLiveData = CompositeLiveData<List<LocationHistoryModelView>>()

    internal val refreshLocationHistoryLiveData =
        CompositeLiveData<List<LocationHistoryModelView>>()

    private var lastTimestamp = -1L

    fun nextLocationHistory() {
        nextLocationHistoryLiveData.add(rxLiveDataTransformer.single(locationHistoryStream()))
    }

    fun refreshLocationHistory() {
        lastTimestamp = -1L
        refreshLocationHistoryLiveData.add(rxLiveDataTransformer.single(locationHistoryStream()))
    }

    private fun locationHistoryStream() = if (lastTimestamp == -1L) {
        userRepo.listLocationHistory()
    } else {
        userRepo.listLocationHistory(lastTimestamp)
    }.map { locationHistories ->
        if (locationHistories.isNotEmpty()) {
            lastTimestamp = locationHistories.minBy { s -> s.timestamp }!!.timestamp
            locationHistories.map { l -> LocationHistoryModelView.newInstance(l) }
        } else {
            listOf()
        }
    }
}