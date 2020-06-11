/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.addresource.select

import com.bitmark.autonomy.data.source.ResourceRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class SelectResourceModule {

    @Provides
    @ActivityScope
    fun provideNav(activity: SelectResourceActivity) = Navigator(activity)

    @Provides
    @ActivityScope
    fun provideDialogController(activity: SelectResourceActivity) = DialogController(activity)

    @Provides
    @ActivityScope
    fun provideVM(
        activity: SelectResourceActivity,
        resourceRepo: ResourceRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = SelectResourceViewModel(activity.lifecycle, resourceRepo, rxLiveDataTransformer)

}