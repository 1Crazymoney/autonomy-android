/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.signout.signout

import androidx.lifecycle.Lifecycle
import com.bitmark.autonomy.data.model.AccountData
import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.data.source.AppRepository
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.util.livedata.CompositeLiveData
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer


class SignOutViewModel(
    lifecycle: Lifecycle,
    private val appRepo: AppRepository,
    private val accountRepo: AccountRepository,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val deleteDataLiveData = CompositeLiveData<Any>()

    internal val getAccountDataLiveData = CompositeLiveData<AccountData>()

    fun deleteData() {
        deleteDataLiveData.add(rxLiveDataTransformer.completable(appRepo.deleteAppData()))
    }

    fun getAccountData() {
        getAccountDataLiveData.add(rxLiveDataTransformer.single(accountRepo.getAccountData()))
    }
}