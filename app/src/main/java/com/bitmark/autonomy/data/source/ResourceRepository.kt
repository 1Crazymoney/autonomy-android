/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.model.ResourceRatingData
import com.bitmark.autonomy.data.source.local.ResourceLocalDataSource
import com.bitmark.autonomy.data.source.remote.ResourceRemoteDataSource
import io.reactivex.Maybe


class ResourceRepository(
    private val remoteDataSource: ResourceRemoteDataSource,
    private val localDataSource: ResourceLocalDataSource
) : Repository {

    fun listResourceRating(poiId: String) = remoteDataSource.listResourceRating(poiId)

    fun updateResourceRatings(ratings: List<ResourceRatingData>) =
        remoteDataSource.updateResourceRatings(ratings)

    fun listImportantResource(poiId: String, lang: String) =
        remoteDataSource.listResource(poiId, lang, true)

    fun listResource(poiId: String, lang: String) =
        Maybe.merge(
            remoteDataSource.listResource(poiId, lang).flatMapMaybe { res ->
                localDataSource.saveResources(poiId, res).andThen(Maybe.just(res))
            }, localDataSource.listResource(poiId).onErrorComplete()
        )

    fun addResources(
        poiId: String,
        existingResourceIds: List<String>,
        newResourceNames: List<String>
    ) =
        remoteDataSource.addResources(poiId, existingResourceIds, newResourceNames)
}