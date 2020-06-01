/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.recovery.notice

import android.text.method.ScrollingMovementMethod
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.model.AccountData
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.recovery.access.RecoveryAccessFragment
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.features.Account
import kotlinx.android.synthetic.main.fragment_recovery_notice.*
import javax.inject.Inject


class RecoveryNoticeFragment : BaseSupportFragment() {

    companion object {
        fun newInstance() = RecoveryNoticeFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: RecoveryNoticeViewModel

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    override fun layoutRes(): Int = R.layout.fragment_recovery_notice

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        tvMsg.movementMethod = ScrollingMovementMethod()

        layoutNext.setSafetyOnclickListener {
            viewModel.getAccountData()
        }

        layoutBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    override fun observe() {
        super.observe()

        viewModel.getAccountDataLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val accountData = res.data()!!
                    loadAccount(accountData) { account ->
                        navigator.anim(RIGHT_LEFT)
                            .replaceFragment(
                                R.id.layoutRoot,
                                RecoveryAccessFragment.newInstance(account.bcRecoveryPhrase.mnemonicWords.toList())
                            )
                    }
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "could not get account data")
                    dialogController.unexpectedAlert { navigator.openIntercom(true) }
                }
            }
        })

    }

    override fun onBackPressed(): Boolean {
        navigator.anim(RIGHT_LEFT).finishActivity()
        return true
    }

    private fun loadAccount(accountData: AccountData, action: (Account) -> Unit) {
        val spec =
            KeyAuthenticationSpec.Builder(context).setKeyAlias(accountData.keyAlias)
                .setAuthenticationDescription(getString(R.string.your_authorization_is_required))
                .setAuthenticationRequired(accountData.authRequired).build()
        activity?.loadAccount(
            accountData.accountNumber,
            spec,
            dialogController,
            successAction = action,
            setupRequiredAction = { navigator.gotoSecuritySetting() },
            invalidErrorAction = { e ->
                logger.logError(Event.ACCOUNT_LOAD_KEY_STORE_ERROR, e)
                dialogController.unexpectedAlert { navigator.openIntercom(true) }
            })
    }

}