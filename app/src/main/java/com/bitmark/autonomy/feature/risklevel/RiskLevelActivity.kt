/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.risklevel

import android.app.AlarmManager
import android.content.Context
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.FADE_IN
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.main.MainActivity
import com.bitmark.autonomy.feature.notification.*
import com.bitmark.autonomy.feature.notification.NotificationHelper.Companion.createChannel
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.logging.Tracer
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.ext.*
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.features.Account
import kotlinx.android.synthetic.main.activity_risk_level.*
import java.util.*
import javax.inject.Inject


class RiskLevelActivity : BaseAppCompatActivity() {

    companion object {
        private const val TAG = "RiskLevelActivity"
    }

    @Inject
    internal lateinit var viewModel: RiskLevelViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private var blocked = false

    override fun layoutRes(): Int = R.layout.activity_risk_level

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        disableDone()

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutDone.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            val account = Account()
            saveAccount(account) { alias ->
                val riskLevel = when {
                    cbRisk.isChecked -> "high"
                    cbNoRisk.isChecked -> "low"
                    else -> error("incorrect risk level")
                }
                viewModel.registerAccount(
                    account,
                    alias,
                    riskLevel,
                    DateTimeUtil.getDefaultTimezone()
                )
            }
        }

        cbRisk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbNoRisk.isChecked = false
                enableDone()
            } else {
                disableDone()
            }
        }

        cbNoRisk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbRisk.isChecked = false
                enableDone()
            } else {
                disableDone()
            }
        }

        layoutRisk.setOnClickListener {
            cbRisk.isChecked = !cbRisk.isChecked
        }

        layoutNoRisk.setOnClickListener {
            cbNoRisk.isChecked = !cbNoRisk.isChecked
        }

        layoutBack.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    override fun observe() {
        super.observe()

        viewModel.registerAccountLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    scheduleNotification()
                    val bundle = MainActivity.getBundle(areas = listOf())
                    navigator.anim(FADE_IN).startActivityAsRoot(MainActivity::class.java, bundle)
                    blocked = false
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.ACCOUNT_REGISTER_ERROR, res.throwable())
                    if (!connectivityHandler.isConnected()) {
                        dialogController.showNoInternetConnection()
                    } else {
                        dialogController.alert(
                            R.string.error,
                            R.string.could_not_register_account
                        )
                    }
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })
    }

    private fun scheduleNotification() {
        // create notification channel for onesignal pushing
        createChannel(this, ChannelId.IMPORTANT_ALERT, true)
        createChannel(this, ChannelId.DEFAULT, false)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // schedule clean&disinfect notification at 9am everyday
        val bundle = NotificationHelper.buildCleanAndDisinfectNotificationBundle(this)
        val pendingIntent = ScheduledNotificationReceiver.getPendingIntent(
            this,
            bundle,
            NotificationId.CLEAN_AND_DISINFECT
        )
        val triggerAt = System.currentTimeMillis() + DateTimeUtil.calculateGapMillisTo(9)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Tracer.DEBUG.log(TAG, "push clean&disinfect notification at: ${Date(triggerAt)}")
    }

    private fun enableDone() {
        layoutDone.enable()
        ivDone.enable()
        tvDone.enable()
    }

    private fun disableDone() {
        layoutDone.disable()
        ivDone.disable()
        tvDone.disable()
    }

    private fun saveAccount(
        account: Account,
        successAction: (String) -> Unit
    ) {
        val keyAlias = account.generateKeyAlias()
        val spec = KeyAuthenticationSpec.Builder(this)
            .setKeyAlias(keyAlias)
            .setAuthenticationRequired(false).build()

        this.saveAccount(
            account,
            spec,
            dialogController,
            successAction = { successAction(keyAlias) },
            setupRequiredAction = { navigator.gotoSecuritySetting() },
            invalidErrorAction = { e ->
                logger.logError(Event.ACCOUNT_SAVE_TO_KEY_STORE_ERROR, e)
                dialogController.alert(
                    getString(R.string.error),
                    e?.message ?: getString(R.string.unexpected_error)
                ) { navigator.openIntercom(true) }
            })
    }

    override fun onBackPressed() {
        if (blocked) return
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}