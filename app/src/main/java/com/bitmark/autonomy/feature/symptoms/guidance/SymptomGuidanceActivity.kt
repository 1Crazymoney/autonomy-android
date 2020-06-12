/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms.guidance

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.behavior.BehaviorReportActivity
import com.bitmark.autonomy.util.ext.openGoogleMapDirection
import com.bitmark.autonomy.util.ext.openPhoneCall
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.modelview.InstitutionModelView
import kotlinx.android.synthetic.main.activity_guidance.rv
import kotlinx.android.synthetic.main.activity_symptom_guidance.*
import javax.inject.Inject


class SymptomGuidanceActivity : BaseAppCompatActivity() {

    companion object {

        private const val INSTITUTIONS = "institutions"

        private const val BEHAVIOR_REPORT_REQUEST_CODE = 0x01

        fun getBundle(institutions: List<InstitutionModelView>) = Bundle().apply {
            putParcelableArrayList(INSTITUTIONS, ArrayList(institutions))
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.activity_symptom_guidance

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val institutions =
            intent?.extras?.getParcelableArrayList<InstitutionModelView>(INSTITUTIONS)
                ?: error("missing institutions")

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.bg_divider)!!)
        rv.addItemDecoration(itemDecoration)
        val adapter = SymptomGuidanceAdapter()
        adapter.set(institutions)
        rv.adapter = adapter

        adapter.setActionListener(object : SymptomGuidanceAdapter.ActionListener {
            override fun onDirectClicked(address: String) {
                navigator.anim(NONE).openGoogleMapDirection(this@SymptomGuidanceActivity, address)
            }

            override fun onPhoneCallClicked(phoneNumber: String) {
                navigator.anim(NONE).openPhoneCall(phoneNumber)
            }

        })

        layoutReportBehavior.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).startActivityForResult(
                BehaviorReportActivity::class.java,
                BEHAVIOR_REPORT_REQUEST_CODE
            )
        }

        layoutDone.setSafetyOnclickListener { navigator.anim(BOTTOM_UP).finishActivity() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == BEHAVIOR_REPORT_REQUEST_CODE) {
            navigator.anim(NONE).finishActivity()
        }
    }

    override fun onBackPressed() {
        navigator.anim(BOTTOM_UP).finishActivity()
        super.onBackPressed()
    }
}