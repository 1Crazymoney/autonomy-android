/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.add2

import com.bitmark.autonomy.data.source.BehaviorRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class BehaviorAdding2Module {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: BehaviorAdding2Activity,
        behaviorRepo: BehaviorRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = BehaviorAdding2ViewModel(activity.lifecycle, behaviorRepo, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNav(activity: BehaviorAdding2Activity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: BehaviorAdding2Activity) = DialogController(activity)
}