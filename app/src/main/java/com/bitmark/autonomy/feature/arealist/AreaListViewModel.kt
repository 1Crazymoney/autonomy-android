/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.arealist

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import io.reactivex.Single


class AreaListViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val renameAreaLiveData = CompositeLiveData<Pair<String, String>>()

    internal val deleteAreaLiveData = CompositeLiveData<String>()

    fun reorder(ids: List<String>) {
        subscribe(userRepo.reorderArea(ids).subscribe({}, {}))
    }

    fun rename(id: String, name: String) {
        renameAreaLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.rename(id, name).andThen(
                    Single.just(Pair(id, name))
                )
            )
        )
    }

    fun delete(id: String) {
        deleteAreaLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.deleteArea(id).andThen(
                    Single.just(
                        id
                    )
                )
            )
        )
    }

}