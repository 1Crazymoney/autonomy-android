/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.ext

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.bitmark.apiservice.utils.callback.Callback0
import com.bitmark.apiservice.utils.callback.Callback1
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.sdk.authentication.KeyAuthenticationSpec
import com.bitmark.sdk.authentication.Provider
import com.bitmark.sdk.authentication.error.AuthenticationException
import com.bitmark.sdk.authentication.error.AuthenticationRequiredException
import com.bitmark.sdk.features.Account
import kotlinx.android.synthetic.main.layout_toast.view.*

fun Activity.hideKeyBoard() {
    val view = this.currentFocus
    if (null != view) {
        val inputManager =
            getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        inputManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

}

fun Activity.showKeyBoard() {
    val view = this.currentFocus
    if (null != view) {
        val inputManager =
            getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        inputManager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

}

fun Activity.saveAccount(
    account: Account,
    spec: KeyAuthenticationSpec,
    dialogController: DialogController,
    successAction: () -> Unit,
    canceledAction: () -> Unit = {},
    setupRequiredAction: () -> Unit = {},
    invalidErrorAction: (Throwable?) -> Unit = {}
) {
    account.saveToKeyStore(
        this,
        spec,
        object : Callback0 {
            override fun onSuccess() {
                successAction()
            }

            override fun onError(throwable: Throwable?) {
                when (throwable) {

                    // authentication error
                    is AuthenticationException -> {
                        when (throwable.type) {
                            // action cancel authentication
                            AuthenticationException.Type.CANCELLED -> {
                                canceledAction.invoke()
                            }

                            else -> {
                                // do nothing
                            }
                        }
                    }

                    // missing security requirement
                    is AuthenticationRequiredException -> {
                        when (throwable.provider) {

                            // did not set up fingerprint/biometric
                            Provider.FINGERPRINT, Provider.BIOMETRIC -> {
                                dialogController.alert(
                                    R.string.error,
                                    R.string.fingerprint_required
                                ) { setupRequiredAction() }
                            }

                            // did not set up pass code
                            else -> {
                                dialogController.alert(
                                    R.string.error,
                                    R.string.passcode_pin_required
                                ) { setupRequiredAction() }
                            }
                        }
                    }
                    else -> {
                        invalidErrorAction.invoke(throwable)
                    }
                }
            }

        })
}

fun Activity.loadAccount(
    accountNumber: String,
    spec: KeyAuthenticationSpec,
    dialogController: DialogController,
    successAction: (Account) -> Unit,
    canceledAction: () -> Unit = {},
    setupRequiredAction: () -> Unit = {},
    invalidErrorAction: (Throwable?) -> Unit = {}
) {
    Account.loadFromKeyStore(
        this,
        accountNumber,
        spec,
        object : Callback1<Account> {
            override fun onSuccess(acc: Account?) {
                successAction.invoke(acc!!)
            }

            override fun onError(throwable: Throwable?) {
                when (throwable) {

                    // authentication error
                    is AuthenticationException -> {
                        when (throwable.type) {
                            // action cancel authentication
                            AuthenticationException.Type.CANCELLED -> {
                                canceledAction.invoke()
                            }

                            else -> {
                                // do nothing
                            }
                        }
                    }

                    // missing security requirement
                    is AuthenticationRequiredException -> {
                        when (throwable.provider) {

                            // did not set up fingerprint/biometric
                            Provider.FINGERPRINT, Provider.BIOMETRIC -> {
                                dialogController.alert(
                                    R.string.error,
                                    R.string.fingerprint_required
                                ) { setupRequiredAction() }
                            }

                            // did not set up pass code
                            else -> {
                                dialogController.alert(
                                    R.string.error,
                                    R.string.passcode_pin_required
                                ) { setupRequiredAction() }
                            }
                        }
                    }
                    else -> {
                        invalidErrorAction.invoke(throwable)
                    }
                }
            }

        })
}

fun Activity.removeAccount(
    accountNumber: String,
    spec: KeyAuthenticationSpec,
    dialogController: DialogController,
    successAction: () -> Unit,
    canceledAction: () -> Unit = {},
    setupRequiredAction: () -> Unit = {},
    invalidErrorAction: (Throwable?) -> Unit = {}
) {
    Account.removeFromKeyStore(this, accountNumber, spec, object : Callback0 {
        override fun onSuccess() {
            successAction.invoke()
        }

        override fun onError(throwable: Throwable?) {
            when (throwable) {

                // authentication error
                is AuthenticationException -> {
                    when (throwable.type) {
                        // action cancel authentication
                        AuthenticationException.Type.CANCELLED -> {
                            canceledAction()
                        }

                        else -> {
                            // do nothing
                        }
                    }
                }

                // missing security requirement
                is AuthenticationRequiredException -> {
                    when (throwable.provider) {

                        // did not set up fingerprint/biometric
                        Provider.FINGERPRINT, Provider.BIOMETRIC -> {
                            dialogController.alert(
                                R.string.error,
                                R.string.fingerprint_required
                            ) { setupRequiredAction() }
                        }

                        // did not set up pass code
                        else -> {
                            dialogController.alert(
                                R.string.error,
                                R.string.passcode_pin_required
                            ) { setupRequiredAction() }
                        }
                    }
                }
                else -> {
                    invalidErrorAction.invoke(throwable)
                }
            }
        }

    })
}

fun Activity.toast(text: String, duration: Int = Toast.LENGTH_SHORT) : Toast {
    val inflater = LayoutInflater.from(this)
    val view = inflater.inflate(R.layout.layout_toast, findViewById(R.id.layoutRoot))
    view.tvContent.text = text
    val toast = Toast(applicationContext)
    toast.setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM, 0, 0)
    toast.duration = duration
    toast.view = view
    toast.show()
    return toast
}

fun Activity.detectKeyBoardState(action: (Boolean) -> Unit, interruptSig: () -> Boolean) {
    val contentView = findViewById<View>(android.R.id.content)
    var isShowing = false
    contentView.viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val rect = Rect()
            contentView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = contentView.rootView.height
            val keyboardHeight = screenHeight - rect.bottom
            if (keyboardHeight > screenHeight * 0.15) {
                if (!isShowing) {
                    isShowing = true
                    action(true)
                }
            } else {
                if (isShowing) {
                    isShowing = false
                    action(false)
                }
            }

            if (interruptSig()) {
                contentView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }

    })
}
