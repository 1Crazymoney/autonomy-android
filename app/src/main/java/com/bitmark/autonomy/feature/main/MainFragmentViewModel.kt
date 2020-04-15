/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.AreaProfileModelView


class MainFragmentViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getAreaProfileLiveData = CompositeLiveData<AreaProfileModelView>()

    fun getCurrentAreaProfile() {
        getAreaProfileLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getCurrentAreaProfile().map { a -> AreaProfileModelView.newInstance(a) }
            )
        )
    }

    fun getAreaProfile(id: String) {
        getAreaProfileLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getAreaProfile(id).map { a -> AreaProfileModelView.newInstance(a) }
            )
        )
    }
}