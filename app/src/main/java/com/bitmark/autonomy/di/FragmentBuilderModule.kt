/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.di

import com.bitmark.autonomy.feature.onboarding.OnboardingFragment
import com.bitmark.autonomy.feature.onboarding.OnboardingModule
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin1Fragment
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin1Module
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin2Fragment
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin2Module
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {

    @ContributesAndroidInjector(modules = [OnboardingModule::class])
    @FragmentScope
    internal abstract fun bindOnBoardingFragment(): OnboardingFragment

    @ContributesAndroidInjector(modules = [SurveyCheckin1Module::class])
    @FragmentScope
    internal abstract fun bindSurveyCheckin1Fragment(): SurveyCheckin1Fragment

    @ContributesAndroidInjector(modules = [SurveyCheckin2Module::class])
    @FragmentScope
    internal abstract fun bindSurveyCheckin2Fragment(): SurveyCheckin2Fragment
}