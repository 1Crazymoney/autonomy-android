/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.signout.notice

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.signout.signout.SignOutFragment
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import kotlinx.android.synthetic.main.fragment_sign_out_notice.*
import javax.inject.Inject


class SignOutNoticeFragment : BaseSupportFragment() {

    companion object {
        fun newInstance() = SignOutNoticeFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.fragment_sign_out_notice

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val msg = SpannableString(getString(R.string.when_you_sign_out_you_will_need))
        val linkText = getString(R.string.please_do_so_now)
        val startIndex = msg.indexOf(linkText)
        val endIndex = startIndex + linkText.length

        msg.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigator.anim(RIGHT_LEFT).finishActivityForResult()
                }

            }, startIndex,
            endIndex,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )

        tvMsg.text = msg
        tvMsg.movementMethod = LinkMovementMethod.getInstance()
        tvMsg.setLinkTextColor(context!!.getColor(R.color.white))
        tvMsg.highlightColor = Color.TRANSPARENT

        layoutNext.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT)
                .replaceFragment(R.id.layoutRoot, SignOutFragment.newInstance(), true)
        }

        layoutBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    override fun onBackPressed(): Boolean {
        navigator.anim(RIGHT_LEFT).finishActivity()
        return true
    }
}