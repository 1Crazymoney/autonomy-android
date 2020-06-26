/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.trending

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.ReportItemModelView


class TrendingViewModel(
    lifecycle: Lifecycle,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listReportItemLiveData = CompositeLiveData<List<ReportItemModelView>>()

    fun listReportItem(
        scope: String,
        type: String,
        start: String,
        end: String,
        poiId: String?,
        lang: String,
        granularity: String
    ) {
        val execFunc = fun(
            scope: String,
            type: String,
            start: String,
            end: String,
            poiId: String?,
            lang: String,
            granularity: String
        ) {
            listReportItemLiveData.add(
                rxLiveDataTransformer.single(
                    appRepo.listReportItem(
                        scope,
                        type,
                        start,
                        end,
                        lang,
                        granularity,
                        poiId
                    ).map { items ->
                        items.map { i ->
                            ReportItemModelView.newInstance(
                                i,
                                type,
                                start
                            )
                        }
                    }
                )
            )
        }
        execFunc(scope, type, start, end, poiId, lang, granularity)
    }
}