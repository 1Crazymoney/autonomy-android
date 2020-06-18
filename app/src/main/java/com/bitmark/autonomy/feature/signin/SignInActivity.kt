/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.signin

import android.os.Handler
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
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
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.livedata.Resource
import com.bitmark.autonomy.util.view.BottomConfirmDialog
import com.bitmark.autonomy.util.view.BottomProgressDialog
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.features.Account
import kotlinx.android.synthetic.main.activity_signin.*
import javax.inject.Inject


class SignInActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: SignInViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private var blocked = false

    private val handler = Handler()

    private lateinit var account: Account

    override fun layoutRes(): Int = R.layout.activity_signin

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        setNextEnable(false)

        etPhrase.imeOptions = EditorInfo.IME_ACTION_DONE
        etPhrase.setRawInputType(InputType.TYPE_CLASS_TEXT)
        etPhrase.requestFocus()
        handler.postDelayed({ showKeyBoard() }, 100)

        layoutNext.setSafetyOnclickListener {
            hideKeyBoard()
            if (blocked) return@setSafetyOnclickListener
            val phrase = etPhrase.text.toString().trim().split(" ").toTypedArray()
            submit(phrase)
        }

        etPhrase.doOnTextChanged { text, _, _, _ ->
            val phrase = text?.trim()?.split(" ")
            if (phrase != null && phrase.size == 13) {
                setNextEnable(true)
            } else {
                setNextEnable(false)
            }
        }

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    private fun setNextEnable(enable: Boolean) {
        if (enable) {
            layoutNext.enable()
            ivNext.enable()
            tvNext.enable()
        } else {
            layoutNext.disable()
            ivNext.disable()
            tvNext.disable()
        }
    }

    private fun submit(phrase: Array<String>) {
        try {
            account = Account.fromRecoveryPhrase(*phrase)
            val authRequired = false
            saveAccount(account, authRequired, successAction = { alias ->
                viewModel.prepareData(account, alias, authRequired)
            }, errorAction = { e ->
                dialogController.alert(e)
            })
        } catch (e: Throwable) {
            showWrongKeyAlert()
        }
    }

    private fun showWrongKeyAlert() {
        val dialog = BottomConfirmDialog(
            this,
            R.string.error,
            R.string.incorrect_recovery_key,
            R.string.you_are_unable_to_sign_in,
            R.string.try_again,
            R.string.cancel,
            negativeClick = {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
        )
        dialog.show()
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.prepareDataLiveData.asLiveData().observe(this, object : Observer<Resource<Any>> {

            private lateinit var progressDialog: BottomProgressDialog

            override fun onChanged(res: Resource<Any>) {
                when {
                    res.isSuccess() -> {
                        handler.postDelayed({
                            progressDialog.dismiss()
                            navigator.anim(FADE_IN).startActivityAsRoot(MainActivity::class.java)
                            blocked = false
                        }, 1000)

                    }
                    res.isError() -> {
                        logger.logError(Event.ACCOUNT_SIGNIN_ERROR, res.throwable())
                        handler.postDelayed({
                            progressDialog.dismiss()
                            if (!connectivityHandler.isConnected()) {
                                dialogController.showNoInternetConnection()
                            } else {
                                dialogController.alert(R.string.error, R.string.could_not_sign_in)
                            }
                            blocked = false
                        }, 1000)
                    }
                    res.isLoading() -> {
                        blocked = true
                        progressDialog = BottomProgressDialog(
                            this@SignInActivity,
                            R.string.signing_in,
                            R.string.please_wait_while_we_sign_you_in
                        )
                        progressDialog.show()
                    }
                }
            }

        })
    }

    private fun saveAccount(
        account: Account,
        authRequired: Boolean,
        successAction: (String) -> Unit,
        errorAction: (Throwable) -> Unit
    ) {
        val keyAlias = account.generateKeyAlias()
        val spec =
            KeyAuthenticationSpec.Builder(this)
                .setKeyAlias(keyAlias)
                .setAuthenticationDescription(getString(R.string.your_authorization_is_required))
                .setAuthenticationRequired(authRequired).build()
        this.saveAccount(
            account,
            spec,
            dialogController,
            successAction = { successAction(keyAlias) },
            setupRequiredAction = {
                navigator.gotoSecuritySetting()
            },
            invalidErrorAction = { e ->
                errorAction(e ?: IllegalAccessException("unknown error"))
                logger.logError(Event.ACCOUNT_SAVE_TO_KEY_STORE_ERROR, e)
            })
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
    }
}