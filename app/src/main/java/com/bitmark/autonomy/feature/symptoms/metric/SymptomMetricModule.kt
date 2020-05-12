/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.metric

import com.bitmark.autonomy.data.source.SymptomRepository
import com.bitmark.autonomy.di.ActivityScope
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.util.livedata.RxLiveDataTransformer
import dagger.Module
import dagger.Provides

@Module
class SymptomMetricModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: SymptomMetricActivity,
        symptomRepo: SymptomRepository,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = SymptomMetricViewModel(activity.lifecycle, symptomRepo, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNav(activity: SymptomMetricActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: SymptomMetricActivity) = DialogController(activity)
}