/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.requesthelp.review

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.requesthelp.RequestHelpData
import com.bitmark.autonomy.feature.requesthelp.resId
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.ext.gone
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.ext.showNoInternetConnection
import com.bitmark.autonomy.util.ext.visible
import com.bitmark.autonomy.util.view.BottomAlertDialog
import kotlinx.android.synthetic.main.fragment_request_help_review.*
import javax.inject.Inject


class RequestHelpReviewFragment : BaseSupportFragment() {

    companion object {

        private const val DATA = "data"

        fun newInstance(data: RequestHelpData): RequestHelpReviewFragment {
            val fragment = RequestHelpReviewFragment()
            val bundle = Bundle()
            bundle.putParcelable(DATA, data)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var viewModel: RequestHelpReviewViewModel

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    @Inject
    internal lateinit var dialogController: DialogController

    private var blocked = false

    private lateinit var data: RequestHelpData

    override fun layoutRes(): Int = R.layout.fragment_request_help_review

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        data = arguments?.getParcelable(DATA) ?: error("missing required data")
    }

    override fun initComponents() {
        super.initComponents()

        tvTime.text = String.format(
            "%s - %s",
            getString(R.string.today),
            DateTimeUtil.now(DateTimeUtil.TIME_FORMAT_1)
        )

        tvType.setText(data.type.resId)

        tvExactNeed.text = data.exactNeed
        tvLocation.text = data.meetingLocation
        tvContactInfo.text = data.contactInfo

        layoutSubmit.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.requestHelp(data)
        }

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).popFragment()
        }
    }

    override fun observe() {
        super.observe()

        viewModel.requestHelpLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val dialog = BottomAlertDialog(
                        context!!,
                        R.string.submitted,
                        R.string.your_request_for_assistance_broadcast,
                        R.string.you_will_receive_notification,
                        R.string.got_it
                    )
                    dialog.setOnDismissListener {
                        navigator.anim(RIGHT_LEFT).finishActivity()
                    }
                    dialog.show()
                    blocked = false
                }

                res.isError() -> {
                    progressBar.gone()

                    if (!connectivityHandler.isConnected()) {
                        dialogController.showNoInternetConnection()
                    } else {
                        dialogController.alert(
                            R.string.error,
                            R.string.could_not_broadcast_your_request
                        )
                    }
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })
    }

    override fun onBackPressed(): Boolean {
        return navigator.anim(RIGHT_LEFT).popFragment()
    }
}