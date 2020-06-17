/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.signout.signout

import android.app.Activity
import android.os.Handler
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.splash.SplashActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.livedata.Resource
import com.bitmark.autonomy.util.view.BottomConfirmDialog
import com.bitmark.autonomy.util.view.BottomProgressDialog
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.features.Account
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.fragment_sign_out.*
import javax.inject.Inject


class SignOutFragment : BaseSupportFragment() {

    companion object {
        fun newInstance() = SignOutFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: SignOutViewModel

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    private var blocked = false

    private val handler = Handler()

    override fun layoutRes(): Int = R.layout.fragment_sign_out

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        setSignOutEnable(false)

        etPhrase.imeOptions = EditorInfo.IME_ACTION_DONE
        etPhrase.setRawInputType(InputType.TYPE_CLASS_TEXT)
        etPhrase.requestFocus()
        activity?.showKeyBoard()

        layoutSignOut.setSafetyOnclickListener {
            activity?.hideKeyBoard()
            if (blocked) return@setSafetyOnclickListener
            try {
                val recoveryKey = etPhrase.text.toString().trim().split(" ").toTypedArray()
                Account.fromRecoveryPhrase(*recoveryKey)
                viewModel.getAccountData()
            } catch (e: Throwable) {
                showWrongKeyAlert()
            }
        }

        etPhrase.doOnTextChanged { text, _, _, _ ->
            val phrase = text?.trim()?.split(" ")
            if (phrase != null && phrase.size == 13) {
                setSignOutEnable(true)
            } else {
                setSignOutEnable(false)
            }
        }

        layoutBack.setOnClickListener {
            if (blocked) return@setOnClickListener
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    private fun setSignOutEnable(enable: Boolean) {
        if (enable) {
            layoutSignOut.enable()
            ivSignOut.enable()
            tvSignOut.enable()
        } else {
            layoutSignOut.disable()
            ivSignOut.disable()
            tvSignOut.disable()
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    private fun showWrongKeyAlert() {
        val dialog = BottomConfirmDialog(
            context!!,
            R.string.error,
            R.string.incorrect_recovery_key,
            R.string.you_are_unable_to_sign_out,
            R.string.try_again,
            R.string.check_key,
            negativeClick = {
                navigator.anim(RIGHT_LEFT).finishActivityForResult()
            })
        dialog.show()
    }

    override fun observe() {
        super.observe()

        viewModel.deleteDataLiveData.asLiveData().observe(this, object : Observer<Resource<Any>> {

            private lateinit var progressDialog: BottomProgressDialog

            override fun onChanged(res: Resource<Any>) {
                when {
                    res.isSuccess() -> {
                        handler.postDelayed({
                            progressDialog.dismiss()
                            OneSignal.setSubscription(false)
                            blocked = false
                            navigator.startActivityAsRoot(SplashActivity::class.java)
                        }, 1000)

                    }

                    res.isError() -> {
                        logger.logError(Event.ACCOUNT_SIGN_OUT_ERROR, res.throwable())
                        handler.postDelayed({
                            progressDialog.dismiss()
                            dialogController.unexpectedAlert { navigator.openIntercom(true) }
                            blocked = false
                        }, 1000)
                    }

                    res.isLoading() -> {
                        blocked = true
                        progressDialog = BottomProgressDialog(
                            context!!,
                            R.string.signing_out,
                            R.string.please_wait_while_we_sign_you_out
                        )
                        progressDialog.show()
                    }
                }
            }
        })

        viewModel.getAccountDataLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val account = res.data()!!
                    removeAccount(
                        activity!!,
                        account.accountNumber,
                        account.keyAlias,
                        dialogController,
                        navigator,
                        {
                            viewModel.deleteData()
                        },
                        {
                            viewModel.deleteData()
                        })
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "sign out load account data error")
                    dialogController.unexpectedAlert { navigator.openIntercom() }
                }
            }
        })
    }

    private fun removeAccount(
        activity: Activity,
        accountNumber: String,
        keyAlias: String,
        dialogController: DialogController,
        navigator: Navigator,
        successAction: () -> Unit,
        invalidAction: () -> Unit
    ) {

        val spec = KeyAuthenticationSpec.Builder(activity.applicationContext)
            .setAuthenticationDescription(getString(R.string.your_authorization_is_required))
            .setKeyAlias(keyAlias).build()
        activity.removeAccount(
            accountNumber,
            spec,
            dialogController,
            successAction = successAction,
            setupRequiredAction = { navigator.gotoSecuritySetting() },
            invalidErrorAction = {
                dialogController.alert(
                    R.string.account_is_not_accessible,
                    R.string.sorry_you_have_changed_or_removed,
                    dismissCallback = invalidAction
                )
            })
    }

    override fun onBackPressed(): Boolean {
        navigator.anim(RIGHT_LEFT).finishActivity()
        return true
    }
}