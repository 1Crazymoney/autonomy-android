/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.di

import com.bitmark.autonomy.feature.arealist.AreaListFragment
import com.bitmark.autonomy.feature.arealist.AreaListModule
import com.bitmark.autonomy.feature.behavior.add.BehaviorAddingFragment
import com.bitmark.autonomy.feature.behavior.add.BehaviorAddingModule
import com.bitmark.autonomy.feature.main.MainFragment
import com.bitmark.autonomy.feature.main.MainFragmentModule
import com.bitmark.autonomy.feature.onboarding.OnboardingFragment
import com.bitmark.autonomy.feature.onboarding.OnboardingModule
import com.bitmark.autonomy.feature.requesthelp.detail.RequestHelpDetailFragment
import com.bitmark.autonomy.feature.requesthelp.detail.RequestHelpDetailModule
import com.bitmark.autonomy.feature.requesthelp.list.RequestHelpListFragment
import com.bitmark.autonomy.feature.requesthelp.list.RequestHelpListModule
import com.bitmark.autonomy.feature.requesthelp.review.RequestHelpReviewFragment
import com.bitmark.autonomy.feature.requesthelp.review.RequestHelpReviewModule
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin1Fragment
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin1Module
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin2Fragment
import com.bitmark.autonomy.feature.survey.checkin.SurveyCheckin2Module
import com.bitmark.autonomy.feature.symptoms.add.SymptomAddingFragment
import com.bitmark.autonomy.feature.symptoms.add.SymptomAddingModule
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

    @ContributesAndroidInjector(modules = [RequestHelpListModule::class])
    @FragmentScope
    internal abstract fun bindRequestHelpListFragment(): RequestHelpListFragment

    @ContributesAndroidInjector(modules = [RequestHelpDetailModule::class])
    @FragmentScope
    internal abstract fun bindRequestHelpDetailFragment(): RequestHelpDetailFragment

    @ContributesAndroidInjector(modules = [RequestHelpReviewModule::class])
    @FragmentScope
    internal abstract fun bindRequestHelpReviewFragment(): RequestHelpReviewFragment

    @ContributesAndroidInjector(modules = [MainFragmentModule::class])
    @FragmentScope
    internal abstract fun bindMainFragment(): MainFragment

    @ContributesAndroidInjector(modules = [AreaListModule::class])
    @FragmentScope
    internal abstract fun bindAreaListFragment(): AreaListFragment

    @ContributesAndroidInjector(modules = [SymptomAddingModule::class])
    @FragmentScope
    internal abstract fun bindSymptomAddingFragment(): SymptomAddingFragment

    @ContributesAndroidInjector(modules = [BehaviorAddingModule::class])
    @FragmentScope
    internal abstract fun bindBehaviorAddingFragment(): BehaviorAddingFragment
}