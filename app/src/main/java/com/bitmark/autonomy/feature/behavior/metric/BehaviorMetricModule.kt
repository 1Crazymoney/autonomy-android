/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.metric

import com.bitmark.autonomy.data.source.BehaviorRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class BehaviorMetricModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: BehaviorMetricActivity,
        behaviorRepo: BehaviorRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = BehaviorMetricViewModel(activity.lifecycle, behaviorRepo, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNav(activity: BehaviorMetricActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: BehaviorMetricActivity) = DialogController(activity)
}