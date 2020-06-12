/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.di

import com.bitmark.autonomy.feature.addresource.add.AddResourceActivity
import com.bitmark.autonomy.feature.addresource.add.AddResourceModule
import com.bitmark.autonomy.feature.addresource.select.SelectResourceActivity
import com.bitmark.autonomy.feature.addresource.select.SelectResourceModule
import com.bitmark.autonomy.feature.areasearch.AreaSearchActivity
import com.bitmark.autonomy.feature.areasearch.AreaSearchModule
import com.bitmark.autonomy.feature.autonomyprofile.AutonomyProfileActivity
import com.bitmark.autonomy.feature.autonomyprofile.AutonomyProfileModule
import com.bitmark.autonomy.feature.behavior.BehaviorReportActivity
import com.bitmark.autonomy.feature.behavior.BehaviorReportModule
import com.bitmark.autonomy.feature.behavior.add.BehaviorAddingContainerActivity
import com.bitmark.autonomy.feature.behavior.add.BehaviorAddingContainerModule
import com.bitmark.autonomy.feature.behavior.add2.BehaviorAdding2Activity
import com.bitmark.autonomy.feature.behavior.add2.BehaviorAdding2Module
import com.bitmark.autonomy.feature.behavior.history.BehaviorHistoryActivity
import com.bitmark.autonomy.feature.behavior.history.BehaviorHistoryModule
import com.bitmark.autonomy.feature.behavior.metric.BehaviorMetricActivity
import com.bitmark.autonomy.feature.behavior.metric.BehaviorMetricModule
import com.bitmark.autonomy.feature.debugmode.DebugModeActivity
import com.bitmark.autonomy.feature.debugmode.DebugModeModule
import com.bitmark.autonomy.feature.donation.DonationActivity
import com.bitmark.autonomy.feature.donation.DonationModule
import com.bitmark.autonomy.feature.guidance.GuidanceActivity
import com.bitmark.autonomy.feature.guidance.GuidanceModule
import com.bitmark.autonomy.feature.locationhistory.LocationHistoryActivity
import com.bitmark.autonomy.feature.locationhistory.LocationHistoryModule
import com.bitmark.autonomy.feature.main.MainActivity
import com.bitmark.autonomy.feature.main.MainActivityModule
import com.bitmark.autonomy.feature.onboarding.OnboardingContainerActivity
import com.bitmark.autonomy.feature.onboarding.OnboardingContainerModule
import com.bitmark.autonomy.feature.permission.PermissionActivity
import com.bitmark.autonomy.feature.permission.PermissionModule
import com.bitmark.autonomy.feature.profile.ProfileActivity
import com.bitmark.autonomy.feature.profile.ProfileModule
import com.bitmark.autonomy.feature.rating.ResourceRatingActivity
import com.bitmark.autonomy.feature.rating.ResourceRatingModule
import com.bitmark.autonomy.feature.recovery.RecoveryContainerActivity
import com.bitmark.autonomy.feature.recovery.RecoveryContainerModule
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
import com.bitmark.autonomy.feature.symptoms.SymptomReportActivity
import com.bitmark.autonomy.feature.symptoms.SymptomReportModule
import com.bitmark.autonomy.feature.symptoms.add.SymptomAddingContainerActivity
import com.bitmark.autonomy.feature.symptoms.add.SymptomAddingContainerModule
import com.bitmark.autonomy.feature.symptoms.add2.SymptomAdding2Activity
import com.bitmark.autonomy.feature.symptoms.add2.SymptomAdding2Module
import com.bitmark.autonomy.feature.symptoms.guidance.SymptomGuidanceActivity
import com.bitmark.autonomy.feature.symptoms.guidance.SymptomGuidanceModule
import com.bitmark.autonomy.feature.symptoms.history.SymptomHistoryActivity
import com.bitmark.autonomy.feature.symptoms.history.SymptomHistoryModule
import com.bitmark.autonomy.feature.symptoms.metric.SymptomMetricActivity
import com.bitmark.autonomy.feature.symptoms.metric.SymptomMetricModule
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

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
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

    @ContributesAndroidInjector(modules = [SymptomReportModule::class])
    @ActivityScope
    internal abstract fun bindSymptomReportActivity(): SymptomReportActivity

    @ContributesAndroidInjector(modules = [BehaviorReportModule::class])
    @ActivityScope
    internal abstract fun bindBehaviorReportActivity(): BehaviorReportActivity

    @ContributesAndroidInjector(modules = [AreaSearchModule::class])
    @ActivityScope
    internal abstract fun bindAreaSearchActivity(): AreaSearchActivity

    @ContributesAndroidInjector(modules = [SymptomAddingContainerModule::class])
    @ActivityScope
    internal abstract fun bindSymptomAddingContainerActivity(): SymptomAddingContainerActivity

    @ContributesAndroidInjector(modules = [BehaviorAddingContainerModule::class])
    @ActivityScope
    internal abstract fun bindBehaviorAddingContainerActivity(): BehaviorAddingContainerActivity

    @ContributesAndroidInjector(modules = [ProfileModule::class])
    @ActivityScope
    internal abstract fun bindProfileActivity(): ProfileActivity

    @ContributesAndroidInjector(modules = [SymptomHistoryModule::class])
    @ActivityScope
    internal abstract fun bindSymptomHistoryActivity(): SymptomHistoryActivity

    @ContributesAndroidInjector(modules = [BehaviorHistoryModule::class])
    @ActivityScope
    internal abstract fun bindBehaviorHistoryActivity(): BehaviorHistoryActivity

    @ContributesAndroidInjector(modules = [LocationHistoryModule::class])
    @ActivityScope
    internal abstract fun bindLocationHistoryActivity(): LocationHistoryActivity

    @ContributesAndroidInjector(modules = [DebugModeModule::class])
    @ActivityScope
    internal abstract fun bindDebugModeActivity(): DebugModeActivity

    @ContributesAndroidInjector(modules = [SymptomAdding2Module::class])
    @ActivityScope
    internal abstract fun bindSymptomAdding2Activity(): SymptomAdding2Activity

    @ContributesAndroidInjector(modules = [SymptomMetricModule::class])
    @ActivityScope
    internal abstract fun bindSymptomMetricActivity(): SymptomMetricActivity

    @ContributesAndroidInjector(modules = [BehaviorAdding2Module::class])
    @ActivityScope
    internal abstract fun bindBehaviorAdding2Activity(): BehaviorAdding2Activity

    @ContributesAndroidInjector(modules = [BehaviorMetricModule::class])
    @ActivityScope
    internal abstract fun bindBehaviorMetricActivity(): BehaviorMetricActivity

    @ContributesAndroidInjector(modules = [GuidanceModule::class])
    @ActivityScope
    internal abstract fun bindGuidanceActivity(): GuidanceActivity

    @ContributesAndroidInjector(modules = [DonationModule::class])
    @ActivityScope
    internal abstract fun bindDonationActivity(): DonationActivity

    @ContributesAndroidInjector(modules = [RecoveryContainerModule::class])
    @ActivityScope
    internal abstract fun bindRecoveryKeyContainerActivity(): RecoveryContainerActivity

    @ContributesAndroidInjector(modules = [AutonomyProfileModule::class])
    @ActivityScope
    internal abstract fun bindAutonomyProfileActivity(): AutonomyProfileActivity

    @ContributesAndroidInjector(modules = [ResourceRatingModule::class])
    @ActivityScope
    internal abstract fun bindRatingActivity(): ResourceRatingActivity

    @ContributesAndroidInjector(modules = [AddResourceModule::class])
    @ActivityScope
    internal abstract fun bindAddResourceActivity(): AddResourceActivity

    @ContributesAndroidInjector(modules = [SelectResourceModule::class])
    @ActivityScope
    internal abstract fun bindSelectResourceActivity(): SelectResourceActivity

    @ContributesAndroidInjector(modules = [SymptomGuidanceModule::class])
    @ActivityScope
    internal abstract fun bindSymptomGuidanceActivity(): SymptomGuidanceActivity

}