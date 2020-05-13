/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.respondhelp

import android.os.Bundle
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.data.source.remote.api.error.HttpException
import com.bitmark.autonomy.data.source.remote.api.error.errorCode
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.requesthelp.Type
import com.bitmark.autonomy.feature.requesthelp.fromString
import com.bitmark.autonomy.feature.requesthelp.resId
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.DateTimeUtil
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.HelpRequestModelView
import com.bitmark.autonomy.util.modelview.isResponded
import com.bitmark.autonomy.util.view.BottomAlertDialog
import kotlinx.android.synthetic.main.activity_respond_help.*
import javax.inject.Inject


class RespondHelpActivity : BaseAppCompatActivity() {

    companion object {
        private const val HELP_REQUEST = "help_request"

        private const val TAG = "RespondHelpActivity"

        fun getBundle(helpRequest: HelpRequestModelView) =
            Bundle().apply { putParcelable(HELP_REQUEST, helpRequest) }
    }

    @Inject
    internal lateinit var viewModel: RespondHelpViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private lateinit var helpRequest: HelpRequestModelView

    private var blocked = false

    override fun layoutRes(): Int = R.layout.activity_respond_help

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getAccountNumber()
    }

    override fun initComponents() {
        super.initComponents()

        helpRequest =
            intent?.extras?.getParcelable(HELP_REQUEST) ?: error("missing required help request")

        ivExactNeedCopy.setSafetyOnclickListener {
            copyToClipboard(helpRequest.exactNeeds)
            toast(getString(R.string.copied).toUpperCase())
        }

        ivLocationDirection.setSafetyOnclickListener {
            navigator.openGoogleMapDirection(this, helpRequest.meetingLocation) {
                toast(getString(R.string.could_not_open_google_map).toUpperCase())
            }
        }

        ivContactInfoCopy.setSafetyOnclickListener {
            copyToClipboard(helpRequest.contactInfo)
            toast(getString(R.string.copied).toUpperCase())
        }

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutSignUp.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.respondHelpRequest(helpRequest.id)
        }

    }

    override fun observe() {
        super.observe()

        viewModel.respondHelpRequestLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val dialog = BottomAlertDialog(
                        this,
                        R.string.signed_up,
                        R.string.thank_u_for_signing_up,
                        R.string.the_community_member_in_need,
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
                    logger.logError(Event.HELP_REQUEST_RESPOND_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        if ((res.throwable() as? HttpException)?.errorCode == 1100) {
                            dialogController.alert(
                                R.string.someone_just_signed_up,
                                R.string.thanks_so_much_for_your_eagerness
                            ) {
                                navigator.anim(RIGHT_LEFT).finishActivity()
                            }
                        } else {
                            dialogController.alert(
                                R.string.error,
                                R.string.could_not_sign_up_request
                            )
                        }
                    } else {
                        dialogController.showNoInternetConnection()
                    }
                    blocked = false
                }

                res.isLoading() -> {
                    blocked = true
                    progressBar.visible()
                }
            }
        })

        viewModel.getAccountNumberLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    showDetailInfo(helpRequest, res.data()!!)
                }

                res.isError() -> {
                    logger.logSharedPrefError(res.throwable(), "$TAG: get account number error")
                    dialogController.unexpectedAlert { navigator.openIntercom(true) }
                }
            }
        })
    }

    private fun showDetailInfo(helpRequest: HelpRequestModelView, accountNumber: String) {

        if (helpRequest.isResponded()) {
            ivBadgeCheck.visible()
            vDivider5.visible()
            vDivider6.visible()
            tvMsg.visible()
            layoutSignUp.gone()

            when (accountNumber) {
                helpRequest.requester -> {
                    tvMsg.setText(R.string.some_one_has_signed_up_requester)
                }

                helpRequest.helper -> {
                    tvMsg.setText(R.string.you_are_currently_signed_up)
                }

                else -> {
                    tvMsg.setText(R.string.some_one_has_signed_up_visitor)
                    vDivider2.gone()
                    vDivider3.gone()
                    vDivider4.gone()
                    tvExactNeedTitle.gone()
                    tvExactNeed.gone()
                    ivExactNeedCopy.gone()
                    tvLocationTitle.gone()
                    tvLocation.gone()
                    ivLocationDirection.gone()
                    tvContactInfoTitle.gone()
                    tvContactInfo.gone()
                    ivContactInfoCopy.gone()
                }
            }
        } else {
            ivBadgeCheck.gone()
            vDivider5.gone()
            vDivider6.gone()
            tvMsg.gone()
            if (accountNumber == helpRequest.requester) {
                layoutSignUp.gone()
            } else {
                layoutSignUp.visible()
            }
        }


        tvTime.text = if (DateTimeUtil.isToday(helpRequest.createdAt)) {
            "%s - %s".format(
                getString(R.string.today),
                DateTimeUtil.stringToString(
                    helpRequest.createdAt,
                    newFormat = DateTimeUtil.TIME_FORMAT_1,
                    newTimezone = DateTimeUtil.getDefaultTimezoneId()
                )
            )
        } else {
            DateTimeUtil.stringToString(helpRequest.createdAt)
        }

        tvType.setText(Type.fromString(helpRequest.subject).resId)
        tvExactNeed.text = helpRequest.exactNeeds
        tvLocation.text = helpRequest.meetingLocation
        tvContactInfo.text = helpRequest.contactInfo
    }


    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}