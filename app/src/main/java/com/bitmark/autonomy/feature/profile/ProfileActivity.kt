/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.profile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.bitmark.autonomy.BuildConfig
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.Navigator.Companion.UP_BOTTOM
import com.bitmark.autonomy.feature.behavior.BehaviorReportActivity
import com.bitmark.autonomy.feature.donation.DonationActivity
import com.bitmark.autonomy.feature.recovery.RecoveryContainerActivity
import com.bitmark.autonomy.feature.signout.SignOutContainerActivity
import com.bitmark.autonomy.feature.symptoms.SymptomReportActivity
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ChromeCustomTabServiceHandler
import com.bitmark.autonomy.util.Constants
import com.bitmark.autonomy.util.ext.*
import kotlinx.android.synthetic.main.activity_profile.*
import javax.inject.Inject


class ProfileActivity : BaseAppCompatActivity() {

    companion object {
        private const val SIGN_OUT_REQUEST_CODE = 0x01
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: ProfileViewModel

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var customTabServiceHandler: ChromeCustomTabServiceHandler

    private var debugModeEnabled = false

    private var handler = Handler()

    override fun layoutRes(): Int = R.layout.activity_profile

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkDebugModeEnable()
    }

    override fun initComponents() {
        super.initComponents()

        val privacyString = getString(R.string.we_protect_your_digital_rights)
        val spannableString = SpannableString(privacyString)
        val ppString = getString(R.string.digital_rights)

        val startIndex = privacyString.indexOf(ppString)
        val endIndex = startIndex + ppString.length
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigator.anim(NONE)
                        .openChromeTab(this@ProfileActivity, Constants.DATA_RIGHTS_URL)
                }

            }, startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        tvPP.setText(spannableString, TextView.BufferType.SPANNABLE)
        tvPP.movementMethod = LinkMovementMethod.getInstance()
        tvPP.setLinkTextColor(getColor(R.color.white))
        tvPP.highlightColor = Color.TRANSPARENT

        val howToKeepDataSpan = SpannableString(getString(R.string.how_we_keep_your_data_private))
        howToKeepDataSpan.setSpan(
            UnderlineSpan(),
            0,
            howToKeepDataSpan.length,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        howToKeepDataSpan.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // TODO handle later
            }

        }, 0, howToKeepDataSpan.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        tvKeepDataPrivate.text = howToKeepDataSpan
        tvKeepDataPrivate.movementMethod = LinkMovementMethod.getInstance()
        tvKeepDataPrivate.setLinkTextColor(getColor(R.color.white))
        tvKeepDataPrivate.highlightColor = Color.TRANSPARENT

        tvVersion.text = BuildConfig.VERSION_NAME

        ivBack.setOnClickListener {
            navigator.anim(UP_BOTTOM).finishActivity()
        }

        layoutReportSymptom.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(SymptomReportActivity::class.java)
        }

        layoutReportBehavior.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(BehaviorReportActivity::class.java)
        }

        layoutSupport.setSafetyOnclickListener {
            navigator.openIntercom()
        }

        layoutDonate.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(DonationActivity::class.java)
        }

        layoutRecoveryKey.setSafetyOnclickListener {
            goToRecoveryKey()
        }

        layoutSignOut.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT)
                .startActivityForResult(SignOutContainerActivity::class.java, SIGN_OUT_REQUEST_CODE)
        }

        var count = 0
        var toast: Toast? = null
        val resetCount = Runnable { count = 0 }
        tvVersion.setOnClickListener {
            if (debugModeEnabled) return@setOnClickListener
            handler.removeCallbacks(resetCount)
            handler.postDelayed(resetCount, 1000)
            ++count
            if (toast != null) {
                toast!!.cancel()
            }
            if (count == 7) {
                viewModel.saveDebugModeState(true)
                count = 0
                debugModeEnabled = true
            } else {
                val remaining = 7 - count
                toast = toast(
                    getString(
                        if (remaining > 1) {
                            R.string.tap_times_to_enable_debug_mode
                        } else {
                            R.string.tap_time_to_enable_debug_mode
                        }
                    ).format(remaining)
                )
            }
        }

        customTabServiceHandler.setUrls(arrayOf(Constants.DATA_RIGHTS_URL))

    }

    override fun onStart() {
        super.onStart()
        customTabServiceHandler.bind()
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.saveDebugModeStateLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    toast(getString(R.string.debug_mode_is_enabled))
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "save debug mode state error")
                }
            }
        })

        viewModel.checkDebugModeEnableLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    debugModeEnabled = res.data()!!
                }

                res.isError() -> {
                    logger.logSharedPrefError(
                        res.throwable(),
                        "profile check debug mode state error"
                    )
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == SIGN_OUT_REQUEST_CODE) {
            goToRecoveryKey()
        }
    }

    private fun goToRecoveryKey() {
        navigator.anim(RIGHT_LEFT).startActivity(RecoveryContainerActivity::class.java)
    }

    override fun onBackPressed() {
        navigator.anim(UP_BOTTOM).finishActivity()
        super.onBackPressed()
    }
}