/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.splash

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
import com.bitmark.autonomy.feature.notification.NotificationId
import com.bitmark.autonomy.feature.notification.NotificationPayloadType
import com.bitmark.autonomy.feature.notification.NotificationType
import com.bitmark.autonomy.feature.onboarding.OnboardingContainerActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.features.Account
import kotlinx.android.synthetic.main.activity_splash.*
import javax.inject.Inject


class SplashActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: SplashViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    override fun layoutRes(): Int = R.layout.activity_splash

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAppInfo()
    }

    override fun initComponents() {
        super.initComponents()

        hideLanding()

        val privacyString = getString(R.string.we_protect_your_digital_rights)
        val spannableString = SpannableString(privacyString)
        val ppString = getString(R.string.digital_rights)

        val startIndex = privacyString.indexOf(ppString)
        val endIndex = startIndex + ppString.length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // TODO change link later
                    navigator.anim(NONE).openBrowser("https://bitmark.com")
                }

            }, startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        tvPP.text = spannableString
        tvPP.movementMethod = LinkMovementMethod.getInstance()
        tvPP.setLinkTextColor(getColor(R.color.white))
        tvPP.highlightColor = Color.TRANSPARENT

        layoutStart.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(OnboardingContainerActivity::class.java)
        }
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
                            viewModel.prepareData(account)
                        }
                    } else {
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
                    val notificationBundle = intent?.extras?.getBundle("notification")
                    if (notificationBundle != null) {
                        var notificationId = notificationBundle.getInt("notification_id")
                        notificationId = if (notificationId == 0) {
                            when (val notificationType =
                                notificationBundle.get(NotificationPayloadType.NOTIFICATION_TYPE)) {
                                NotificationType.NEW_HELP_REQUEST -> NotificationId.NEW_HELP_REQUEST
                                NotificationType.ACCEPTED_HELP_REQUEST -> NotificationId.ACCEPTED_HELP_REQUEST
                                NotificationType.RISK_LEVEL_CHANGED -> NotificationId.RISK_LEVEL_CHANGED
                                else -> {
                                    logger.logError(
                                        Event.NOTIFICATION_HANDLING_ERROR,
                                        "invalid notification type: $notificationType"
                                    )
                                    0
                                }
                            }
                        } else {
                            notificationId
                        }
                        notificationBundle.putInt("notification_id", notificationId)
                    }
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

    private fun showLanding() {
        vToolbar1.visible()
        vToolbar2.visible()
        tvTitle.visible()
        tvPP.visible()
        layoutStart.visible()
        ivSecureByBm.invisible()
    }

    private fun hideLanding() {
        vToolbar1.invisible()
        vToolbar2.invisible()
        tvTitle.invisible()
        tvPP.invisible()
        layoutStart.invisible()
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