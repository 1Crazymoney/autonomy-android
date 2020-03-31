/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.requesthelp.review

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.AssistanceRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.requesthelp.RequestHelpData
import com.bitmark.autonomy.feature.requesthelp.value
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer


class RequestHelpReviewViewModel(
    lifecycle: Lifecycle,
    private val assistanceRepo: AssistanceRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val requestHelpLiveData = CompositeLiveData<Any>()

    fun requestHelp(data: RequestHelpData) {
        requestHelpLiveData.add(
            rxLiveDataTransformer.completable(
                assistanceRepo.requestHelp(
                    data.type.value,
                    data.exactNeed,
                    data.meetingLocation,
                    data.contactInfo
                )
            )
        )
    }

}