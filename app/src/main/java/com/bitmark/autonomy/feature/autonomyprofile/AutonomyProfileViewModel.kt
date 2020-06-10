/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.autonomyprofile

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.UserRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import com.bitmark.autonomy.util.modelview.AutonomyProfileModelView


class AutonomyProfileViewModel(
    lifecycle: Lifecycle,
    private val userRepo: UserRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val getAreaProfileLiveData = CompositeLiveData<AutonomyProfileModelView>()

    fun getYourAutonomyProfile() {
        getAreaProfileLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getYourAutonomyProfile().map { a -> AutonomyProfileModelView.newInstance(a) }
            )
        )
    }

    fun getAutonomyProfile(id: String, allResources: Boolean = false) {
        getAreaProfileLiveData.add(
            rxLiveDataTransformer.single(
                userRepo.getAutonomyProfile(
                    id,
                    allResources
                ).map { a -> AutonomyProfileModelView.newInstance(a) }
            )
        )
    }

}