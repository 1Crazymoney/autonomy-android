/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.profile

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
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
import com.bitmark.autonomy.feature.symptoms.SymptomReportActivity
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import kotlinx.android.synthetic.main.activity_profile.*
import javax.inject.Inject


class ProfileActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: ProfileViewModel

    @Inject
    internal lateinit var logger: EventLogger

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

    override fun onBackPressed() {
        navigator.anim(UP_BOTTOM).finishActivity()
        super.onBackPressed()
    }
}