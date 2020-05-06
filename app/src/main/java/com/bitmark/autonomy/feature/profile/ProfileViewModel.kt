/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.profile

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer


class ProfileViewModel(
    lifecycle: Lifecycle,
    private val appRepo: AppRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val checkDebugModeEnableLiveData = CompositeLiveData<Boolean>()

    internal val saveDebugModeStateLiveData = CompositeLiveData<Any>()

    fun checkDebugModeEnable() {
        checkDebugModeEnableLiveData.add(rxLiveDataTransformer.single(appRepo.checkDebugModeEnable()))
    }

    fun saveDebugModeState(enable: Boolean) {
        saveDebugModeStateLiveData.add(
            rxLiveDataTransformer.completable(
                appRepo.saveDebugModeState(
                    enable
                )
            )
        )
    }

}