/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.risklevel

import com.bitmark.autonomy.data.source.AccountRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class RiskLevelModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: RiskLevelActivity,
        accountRepo: AccountRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = RiskLevelViewModel(activity.lifecycle, accountRepo, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNav(activity: RiskLevelActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: RiskLevelActivity) = DialogController(activity)

}