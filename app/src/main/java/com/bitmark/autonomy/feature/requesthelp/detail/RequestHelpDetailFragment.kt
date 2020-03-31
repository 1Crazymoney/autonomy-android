/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.requesthelp.detail

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.core.widget.doOnTextChanged
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.requesthelp.RequestHelpData
import com.bitmark.autonomy.feature.requesthelp.review.RequestHelpReviewFragment
import com.bitmark.autonomy.util.ext.disable
import com.bitmark.autonomy.util.ext.enable
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.ext.showKeyBoard
import kotlinx.android.synthetic.main.fragment_request_help_detail.*
import javax.inject.Inject


class RequestHelpDetailFragment : BaseSupportFragment() {

    companion object {

        private const val DATA = "data"

        private const val TYPE = "type"

        enum class Type {
            EXACT_NEED, MEETING_LOCATION, CONTACT_INFO;

            companion object
        }

        val Type.value: String
            get() = when (this) {
                Type.EXACT_NEED -> "exact_need"
                Type.MEETING_LOCATION -> "meeting_location"
                Type.CONTACT_INFO -> "contact_info"
            }

        fun Type.Companion.fromString(type: String) = when (type) {
            "exact_need" -> Type.EXACT_NEED
            "meeting_location" -> Type.MEETING_LOCATION
            "contact_info" -> Type.CONTACT_INFO
            else -> error("invalid type: $type")
        }

        fun newInstance(data: RequestHelpData, type: Type): RequestHelpDetailFragment {
            val fragment = RequestHelpDetailFragment()
            val bundle = Bundle()
            bundle.putParcelable(DATA, data)
            bundle.putString(TYPE, type.value)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    private lateinit var data: RequestHelpData

    private lateinit var type: Type

    private val handler = Handler()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        data = arguments?.getParcelable(DATA) ?: error("missing required data")
        type = Type.fromString(arguments?.getString(TYPE) ?: error("missing required type"))
    }

    override fun layoutRes(): Int = R.layout.fragment_request_help_detail

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        disableNext()
        etDetail.requestFocus()

        handler.postDelayed({ activity?.showKeyBoard() }, 200)

        if (type == Type.CONTACT_INFO) {
            tvNext.setText(R.string.review)
        } else {
            tvNext.setText(R.string.next)
        }

        tvSlogan.setText(
            when (type) {
                Type.EXACT_NEED -> R.string.what_exactly_do_u_need
                Type.MEETING_LOCATION -> R.string.where_is_safe_place_to_meet
                Type.CONTACT_INFO -> R.string.how_should_your_helper_contact_u
            }
        )

        etDetail.setHint(
            when (type) {
                Type.EXACT_NEED -> R.string.enter_description_if_desired_items
                Type.MEETING_LOCATION -> R.string.enter_physical_address_other_safe_location
                Type.CONTACT_INFO -> R.string.enter_contact_info_here
            }
        )

        etDetail.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                disableNext()
            } else {
                enableNext()
            }
        }

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).popFragment()
        }

        layoutNext.setSafetyOnclickListener {
            val content = etDetail.text.toString().trim()
            when (type) {
                Type.EXACT_NEED -> {
                    data.exactNeed = content
                    navigator.anim(RIGHT_LEFT).replaceFragment(
                        R.id.layoutContainer,
                        newInstance(data, Type.MEETING_LOCATION),
                        true
                    )
                }
                Type.MEETING_LOCATION -> {
                    data.meetingLocation = content
                    navigator.anim(RIGHT_LEFT).replaceFragment(
                        R.id.layoutContainer,
                        newInstance(data, Type.CONTACT_INFO),
                        true
                    )
                }
                Type.CONTACT_INFO -> {
                    data.contactInfo = content
                    navigator.anim(RIGHT_LEFT).replaceFragment(
                        R.id.layoutContainer,
                        RequestHelpReviewFragment.newInstance(data),
                        true
                    )
                }
            }
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    private fun disableNext() {
        layoutNext.disable()
        ivNext.disable()
        tvNext.disable()
    }

    private fun enableNext() {
        layoutNext.enable()
        ivNext.enable()
        tvNext.enable()
    }

    override fun onBackPressed(): Boolean {
        return navigator.anim(RIGHT_LEFT).popFragment()
    }
}