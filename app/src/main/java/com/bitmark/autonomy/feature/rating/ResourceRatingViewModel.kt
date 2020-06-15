/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.rating

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.ResourceRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.ResourceRatingModelView
import com.bitmark.autonomy.util.modelview.toResourceRatingData


class ResourceRatingViewModel(
    lifecycle: Lifecycle,
    private val resourceRepo: ResourceRepository,
    val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val listResourceRatingLiveData = CompositeLiveData<List<ResourceRatingModelView>>()

    internal val updateResourceRatingsLiveData = CompositeLiveData<Any>()

    fun listResourceRating(poiId: String, lang: String) {
        listResourceRatingLiveData.add(
            rxLiveDataTransformer.single(
                resourceRepo.listResourceRating(
                    poiId,
                    lang
                ).map { ratings -> ratings.map { r -> ResourceRatingModelView.newInstance(r) } })
        )
    }

    fun updateResourceRatings(poiId: String, ratings: List<ResourceRatingModelView>) {
        updateResourceRatingsLiveData.add(
            rxLiveDataTransformer.completable(
                resourceRepo.updateResourceRatings(poiId,
                    ratings.map { r -> r.toResourceRatingData() })
            )
        )
    }
}