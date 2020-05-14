/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.donation

import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.util.ext.disable
import com.bitmark.autonomy.util.ext.enable
import com.bitmark.autonomy.util.ext.openChromeTab
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import kotlinx.android.synthetic.main.activity_donation.*
import kotlinx.android.synthetic.main.item_checkable.view.*
import javax.inject.Inject


class DonationActivity : BaseAppCompatActivity() {

    companion object {
        private const val PAYPAL_ME = "https://paypal.me/AutonomyByBitmark/"
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_donation

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val layouts = arrayOf(layout1, layout5, layout20, layoutOther)

        layouts.forEach { l ->
            when (l.id) {
                R.id.layout1 -> {
                    l.cb.isChecked = false
                    l.tv.text = "$1"
                }

                R.id.layout5 -> {
                    l.cb.isChecked = true
                    l.tv.text = "$5"
                }

                R.id.layout20 -> {
                    l.cb.isChecked = false
                    l.tv.text = "$20"
                }

                R.id.layoutOther -> {
                    l.cb.isChecked = false
                    l.tv.setText(R.string.other_amount)
                }
            }

            l.setOnClickListener {
                l.cb.isChecked = !l.cb.isChecked
            }

            l.cb.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    layouts.filter { it != l && it.cb.isChecked }
                        .forEach { it.cb.isChecked = false }
                    enableNext()
                } else {
                    disableNext()
                }
            }
        }

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutNext.setSafetyOnclickListener {
            val checkedText = layouts.first { it.cb.isChecked }.tv.text.toString()
            val url = if (checkedText.contains("$")) {
                PAYPAL_ME + checkedText.removePrefix("$") + "usd"
            } else {
                PAYPAL_ME
            }
            navigator.anim(NONE).openChromeTab(this, url)
        }
    }

    private fun enableNext() {
        tvNext.enable()
        ivNext.enable()
        layoutNext.enable()
    }

    private fun disableNext() {
        tvNext.disable()
        ivNext.disable()
        layoutNext.disable()
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}