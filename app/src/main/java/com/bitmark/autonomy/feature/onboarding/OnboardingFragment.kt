/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.location.LocationService
import com.bitmark.autonomy.feature.permission.PermissionActivity
import com.bitmark.autonomy.feature.risklevel.RiskLevelActivity
import com.bitmark.autonomy.util.ext.getDimensionPixelSize
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.fragment_onboarding_2.*
import kotlinx.android.synthetic.main.item_symptom.view.*
import javax.inject.Inject


class OnboardingFragment : BaseSupportFragment() {

    companion object {

        private val onboardingLayoutRes = arrayOf(
            R.layout.fragment_onboarding_1,
            R.layout.fragment_onboarding_2,
            R.layout.fragment_onboarding_3
        )

        private const val LAYOUT_RES = "layout_res"

        fun newInstance(@LayoutRes layoutRes: Int = onboardingLayoutRes[0]): Fragment {
            val fragment = OnboardingFragment()
            val bundle = Bundle()
            bundle.putInt(LAYOUT_RES, layoutRes)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var locationService: LocationService

    private var layoutId: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        layoutId = arguments?.getInt(LAYOUT_RES) ?: error("missing layout id")
    }

    override fun layoutRes(): Int = layoutId!!

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        if (layoutId == R.layout.fragment_onboarding_2) {
            val symptoms = resources.getStringArray(R.array.symptoms)
            layoutSymptom.removeAllViews()
            symptoms.forEach { s ->
                val view = LayoutInflater.from(context).inflate(R.layout.item_symptom, null)
                view.tvSymptom.text = s
                val params = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                val margin = context!!.getDimensionPixelSize(R.dimen.dp_8)
                params.setMargins(margin, margin, margin, margin)
                view.layoutParams = params
                layoutSymptom.addView(view)
            }
        }

        val btnBack = view!!.findViewById<LinearLayout>(R.id.layoutBack)
        val btnNext = view!!.findViewById<LinearLayout>(R.id.layoutNext)

        btnBack.setSafetyOnclickListener {
            if (layoutId == onboardingLayoutRes.first()) {
                navigator.anim(RIGHT_LEFT).finishActivity()
            } else {
                navigator.anim(RIGHT_LEFT).popFragment()
            }

        }

        btnNext.setSafetyOnclickListener {
            val index = onboardingLayoutRes.indexOf(layoutId)
            if (index == onboardingLayoutRes.size - 1) {
                // last item

                if (locationService.isPermissionGranted(activity!!)) {
                    navigator.anim(RIGHT_LEFT).startActivity(RiskLevelActivity::class.java)
                } else {
                    navigator.anim(RIGHT_LEFT).startActivity(PermissionActivity::class.java)
                }
            } else {
                navigator.anim(RIGHT_LEFT)
                    .replaceFragment(
                        R.id.layoutContainer,
                        newInstance(onboardingLayoutRes[index + 1])
                    )
            }
        }

    }

    override fun onBackPressed(): Boolean {
        return navigator.popFragment()
    }
}