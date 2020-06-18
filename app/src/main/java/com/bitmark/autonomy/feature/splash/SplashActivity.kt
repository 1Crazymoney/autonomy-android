/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.splash

import android.app.AlarmManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.Observer
import com.bitmark.autonomy.BuildConfig
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.AccountData
import com.bitmark.autonomy.data.model.isRegistered
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.FADE_IN
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.main.MainActivity
import com.bitmark.autonomy.feature.notification.NotificationHelper
import com.bitmark.autonomy.feature.notification.NotificationId
import com.bitmark.autonomy.feature.notification.ScheduledNotificationReceiver
import com.bitmark.autonomy.feature.notification.buildCleanAndDisinfectNotificationBundle
import com.bitmark.autonomy.feature.onboarding.OnboardingContainerActivity
import com.bitmark.autonomy.feature.signin.SignInActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ChromeCustomTabServiceHandler
import com.bitmark.autonomy.util.Constants
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.ext.*
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.features.Account
import io.intercom.android.sdk.Intercom
import kotlinx.android.synthetic.main.activity_splash.*
import javax.inject.Inject


class SplashActivity : BaseAppCompatActivity() {

    companion object {

        private const val NOTIFICATION_BUNDLE = "notification_bundle"

        fun getBundle(notificationBundle: Bundle? = null) =
            Bundle().apply {
                if (notificationBundle != null) {
                    putBundle(
                        NOTIFICATION_BUNDLE,
                        notificationBundle
                    )
                }
            }
    }

    @Inject
    internal lateinit var viewModel: SplashViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var customTabServiceHandler: ChromeCustomTabServiceHandler

    override fun layoutRes(): Int = R.layout.activity_splash

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAppInfo()
    }

    override fun onStart() {
        super.onStart()
        customTabServiceHandler.bind()
    }

    override fun initComponents() {
        super.initComponents()

        hideLanding()

        val protectDataRightString = getString(R.string.we_protect_your_digital_rights)
        val dataRightSpannableString = SpannableString(protectDataRightString)
        val dataRightString = getString(R.string.digital_rights)

        var startIndex = protectDataRightString.indexOf(dataRightString)
        if (startIndex != -1) {
            dataRightSpannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        navigator.anim(NONE)
                            .openChromeTab(this@SplashActivity, Constants.DATA_RIGHTS_URL)
                    }

                }, startIndex,
                startIndex + dataRightString.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        tvDataRight.text = dataRightSpannableString
        tvDataRight.movementMethod = LinkMovementMethod.getInstance()
        tvDataRight.setLinkTextColor(getColor(R.color.concord))
        tvDataRight.highlightColor = Color.TRANSPARENT

        val signInText = getString(R.string.or_sign_back_in)
        val signInSpannableString = SpannableString(signInText)
        val subSignInText = getString(R.string.sign_back_in)

        startIndex = signInText.indexOf(subSignInText)
        if (startIndex != -1) {
            signInSpannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        navigator.anim(RIGHT_LEFT).startActivity(SignInActivity::class.java)
                    }

                }, startIndex,
                startIndex + subSignInText.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }

        tvSingIn.text = signInSpannableString
        tvSingIn.movementMethod = LinkMovementMethod.getInstance()
        tvSingIn.setLinkTextColor(getColor(R.color.concord))
        tvSingIn.highlightColor = Color.TRANSPARENT

        layoutStart.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(OnboardingContainerActivity::class.java)
        }

        customTabServiceHandler.setUrls(arrayOf(Constants.DATA_RIGHTS_URL))
    }

    override fun observe() {
        super.observe()

        viewModel.getAppInfoLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val appInfoData = res.data()!!
                    val versionOutOfDate =
                        BuildConfig.VERSION_CODE < appInfoData.androidAppInfo.requiredVersion
                    if (versionOutOfDate) {
                        dialogController.showUpdateRequired {
                            val updateUrl = appInfoData.androidAppInfo.updateUrl
                            navigator.goToUpdateApp(updateUrl)
                            navigator.exitApp()
                        }
                    } else {
                        viewModel.getAccountData()
                    }
                }

                res.isError() -> {
                    logger.logError(Event.APP_GET_INFO_ERROR, res.throwable())
                    viewModel.getAccountData()
                }
            }
        })

        viewModel.getAccountDataLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val accountData = res.data()!!
                    if (accountData.isRegistered()) {
                        loadAccount(accountData) { account ->
                            viewModel.prepareData(account, DateTimeUtil.getDefaultTimezone())
                        }
                    } else {
                        Intercom.client().registerUnidentifiedUser()
                        showLanding()
                    }
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "splash_check_account_registered")
                    dialogController.unexpectedAlert { navigator.openIntercom(true) }
                }
            }
        })

        viewModel.prepareDataLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val notificationBundle = intent?.extras?.getBundle(NOTIFICATION_BUNDLE)
                    scheduleNotificationIfNeeded()
                    val bundle = MainActivity.getBundle(notificationBundle)
                    navigator.anim(FADE_IN)
                        .startActivityAsRoot(MainActivity::class.java, bundle)
                }

                res.isError() -> {
                    logger.logError(Event.ACCOUNT_JWT_ERROR, res.throwable())
                }
            }
        })
    }

    private fun scheduleNotificationIfNeeded() {
        val bundle = NotificationHelper.buildCleanAndDisinfectNotificationBundle(this)
        val isActive =
            ScheduledNotificationReceiver.isActive(this, bundle, NotificationId.CLEAN_AND_DISINFECT)
        if (isActive) return
        val pendingIntent = ScheduledNotificationReceiver.getPendingIntent(
            this,
            bundle,
            NotificationId.CLEAN_AND_DISINFECT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = DateTimeUtil.calculateGapMillisTo(9)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun showLanding() {
        vToolbar1.visible()
        vToolbar2.visible()
        tvTitle.visible()
        tvDataRight.visible()
        layoutStart.visible()
        tvSingIn.visible()
        ivSecureByBm.invisible()
    }

    private fun hideLanding() {
        vToolbar1.invisible()
        vToolbar2.invisible()
        tvTitle.invisible()
        tvDataRight.invisible()
        layoutStart.invisible()
        tvSingIn.invisible()
        ivSecureByBm.visible()
    }

    private fun loadAccount(accountData: AccountData, action: (Account) -> Unit) {
        val spec =
            KeyAuthenticationSpec.Builder(this).setKeyAlias(accountData.keyAlias)
                .setAuthenticationDescription(getString(R.string.your_authorization_is_required))
                .setAuthenticationRequired(accountData.authRequired).build()
        loadAccount(
            accountData.accountNumber,
            spec,
            dialogController,
            successAction = action,
            setupRequiredAction = { navigator.gotoSecuritySetting() },
            canceledAction = {
                dialogController.showAuthRequired {
                    loadAccount(accountData, action)
                }
            },
            invalidErrorAction = { e ->
                logger.logError(Event.ACCOUNT_LOAD_KEY_STORE_ERROR, e)
                dialogController.alert(e) { navigator.exitApp() }
            })
    }


}