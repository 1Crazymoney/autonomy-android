/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.requesthelp.list

import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.requesthelp.RequestHelpData
import com.bitmark.autonomy.feature.requesthelp.Type
import com.bitmark.autonomy.feature.requesthelp.detail.RequestHelpDetailFragment
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import kotlinx.android.synthetic.main.fragment_request_help_list.*
import javax.inject.Inject


class RequestHelpListFragment : BaseSupportFragment() {

    companion object {
        fun newInstance() = RequestHelpListFragment()
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.fragment_request_help_list

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        layoutFood.setSafetyOnclickListener {
            goToDetail(Type.FOOD)
        }

        layoutMedicine.setSafetyOnclickListener {
            goToDetail(Type.MEDICINE)
        }

        layoutMedicalCare.setSafetyOnclickListener {
            goToDetail(Type.MEDICAL_CARE)
        }

        layoutSafeLocation.setSafetyOnclickListener {
            goToDetail(Type.SAFE_LOCATION)
        }
    }

    private fun goToDetail(type: Type) {
        val data = RequestHelpData(type)
        navigator.anim(RIGHT_LEFT).replaceFragment(
            R.id.layoutContainer,
            RequestHelpDetailFragment.newInstance(
                data,
                RequestHelpDetailFragment.Companion.Type.EXACT_NEED
            ),
            true
        )
    }

    override fun onBackPressed(): Boolean {
        return navigator.popFragment()
    }
}