/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.di

import com.bitmark.autonomy.feature.onboarding.OnboardingFragment
import com.bitmark.autonomy.feature.onboarding.OnboardingModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector(modules = [OnboardingModule::class])
    @FragmentScope
    internal abstract fun bindOnBoardingFragment(): OnboardingFragment
}