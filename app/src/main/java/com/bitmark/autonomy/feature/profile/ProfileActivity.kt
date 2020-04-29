/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.profile

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.bitmark.autonomy.BuildConfig
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.Navigator.Companion.UP_BOTTOM
import com.bitmark.autonomy.feature.behavior.BehaviorReportActivity
import com.bitmark.autonomy.feature.behavior.history.BehaviorHistoryActivity
import com.bitmark.autonomy.feature.locationhistory.LocationHistoryActivity
import com.bitmark.autonomy.feature.symptoms.SymptomReportActivity
import com.bitmark.autonomy.feature.symptoms.history.SymptomHistoryActivity
import com.bitmark.autonomy.util.ext.openBrowser
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import kotlinx.android.synthetic.main.activity_profile.*
import javax.inject.Inject


class ProfileActivity : BaseAppCompatActivity() {

    @Inject
    lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_profile

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val eulaAndPpString = getString(R.string.eula_and_pp)
        val spannableString = SpannableString(eulaAndPpString)
        val eulaString = getString(R.string.eula)
        val ppString = getString(R.string.privacy_policy)

        var startIndex = eulaAndPpString.indexOf(eulaString)
        var endIndex = startIndex + eulaString.length
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

        startIndex = eulaAndPpString.indexOf(ppString)
        endIndex = startIndex + ppString.length
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

        tvEulaPP.text = spannableString
        tvEulaPP.movementMethod = LinkMovementMethod.getInstance()
        tvEulaPP.setLinkTextColor(getColor(R.color.white))
        tvEulaPP.highlightColor = Color.TRANSPARENT

        tvVersion.text = BuildConfig.VERSION_NAME

        ivBack.setOnClickListener {
            navigator.anim(UP_BOTTOM).finishActivity()
        }

        layoutReportSymptom.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(SymptomReportActivity::class.java)
        }

        layoutHistorySymptom.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(SymptomHistoryActivity::class.java)
        }

        layoutReportBehavior.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(BehaviorReportActivity::class.java)
        }

        layoutHistoryBehavior.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(BehaviorHistoryActivity::class.java)
        }

        layoutHistoryLocation.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivity(LocationHistoryActivity::class.java)
        }

    }

    override fun onBackPressed() {
        navigator.anim(UP_BOTTOM).finishActivity()
        super.onBackPressed()
    }
}