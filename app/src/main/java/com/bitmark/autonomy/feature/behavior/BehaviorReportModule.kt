/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior

import com.bitmark.autonomy.data.source.BehaviorRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class BehaviorReportModule {

    @Provides
    @ActivityScope
    fun provideNav(activity: BehaviorReportActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideDialogController(activity: BehaviorReportActivity) = DialogController(activity)

    @Provides
    @ActivityScope
    fun provideVM(
        activity: BehaviorReportActivity,
        behaviorRepo: BehaviorRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = BehaviorReportViewModel(activity.lifecycle, behaviorRepo, rxLiveDataTransformer)
}