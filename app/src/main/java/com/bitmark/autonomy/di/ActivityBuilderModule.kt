/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.di

import com.bitmark.autonomy.feature.main.MainActivity
import com.bitmark.autonomy.feature.main.MainModule
import com.bitmark.autonomy.feature.onboarding.OnboardingContainerActivity
import com.bitmark.autonomy.feature.onboarding.OnboardingContainerModule
import com.bitmark.autonomy.feature.permission.PermissionActivity
import com.bitmark.autonomy.feature.permission.PermissionModule
import com.bitmark.autonomy.feature.requesthelp.RequestHelpContainerActivity
import com.bitmark.autonomy.feature.requesthelp.RequestHelpContainerModule
import com.bitmark.autonomy.feature.respondhelp.RespondHelpActivity
import com.bitmark.autonomy.feature.respondhelp.RespondHelpModule
import com.bitmark.autonomy.feature.risklevel.RiskLevelActivity
import com.bitmark.autonomy.feature.risklevel.RiskLevelModule
import com.bitmark.autonomy.feature.splash.SplashActivity
import com.bitmark.autonomy.feature.splash.SplashModule
import com.bitmark.autonomy.feature.survey.SurveyContainerActivity
import com.bitmark.autonomy.feature.survey.SurveyContainerModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = [OnboardingContainerModule::class])
    @ActivityScope
    internal abstract fun bindOnBoardingActivity(): OnboardingContainerActivity

    @ContributesAndroidInjector(modules = [SplashModule::class])
    @ActivityScope
    internal abstract fun bindSplashActivity(): SplashActivity

    @ContributesAndroidInjector(modules = [PermissionModule::class])
    @ActivityScope
    internal abstract fun bindPermissionActivity(): PermissionActivity

    @ContributesAndroidInjector(modules = [RiskLevelModule::class])
    @ActivityScope
    internal abstract fun bindRiskLevelActivity(): RiskLevelActivity

    @ContributesAndroidInjector(modules = [MainModule::class])
    @ActivityScope
    internal abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [SurveyContainerModule::class])
    @ActivityScope
    internal abstract fun bindSurveyContainerActivity(): SurveyContainerActivity

    @ContributesAndroidInjector(modules = [RequestHelpContainerModule::class])
    @ActivityScope
    internal abstract fun bindRequestHelpContainerActivity(): RequestHelpContainerActivity

    @ContributesAndroidInjector(modules = [RespondHelpModule::class])
    @ActivityScope
    internal abstract fun bindRespondHelpActivity(): RespondHelpActivity

}