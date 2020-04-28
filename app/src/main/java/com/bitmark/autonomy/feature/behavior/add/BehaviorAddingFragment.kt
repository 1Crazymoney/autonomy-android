/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.add

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.BehaviorModelView
import com.bitmark.autonomy.util.view.BottomAlertDialog
import kotlinx.android.synthetic.main.fragment_symptom_adding.*
import javax.inject.Inject


class BehaviorAddingFragment : BaseSupportFragment() {

    companion object {

        private const val DATA = "data"

        private const val TYPE = "type"

        enum class Type {
            TITLE, DESCRIPTION;

            companion object
        }

        val Type.value: String
            get() = when (this) {
                Type.TITLE -> "title"
                Type.DESCRIPTION -> "des"
            }

        fun Type.Companion.fromString(type: String) = when (type) {
            "title" -> Type.TITLE
            "des" -> Type.DESCRIPTION
            else -> error("invalid type: $type")
        }

        fun newInstance(
            data: NewBehaviorData = NewBehaviorData(),
            type: Type = Type.TITLE
        ): BehaviorAddingFragment {
            val fragment = BehaviorAddingFragment()
            val bundle = Bundle().apply {
                putParcelable(DATA, data)
                putString(TYPE, type.value)
            }
            fragment.arguments = bundle
            return fragment
        }

        fun extractResultData(data: Intent?) = data?.getParcelableExtra<BehaviorModelView>(DATA)
    }

    @Inject
    internal lateinit var viewModel: BehaviorAddingViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private lateinit var data: NewBehaviorData

    private lateinit var type: Type

    private val handler = Handler()

    private var blocked = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        data = arguments?.getParcelable(DATA) ?: error("missing required data")
        type = Type.fromString(arguments?.getString(TYPE) ?: error("missing required type"))
    }

    override fun layoutRes(): Int = R.layout.fragment_behavior_adding

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        disableFinish()
        etDetail.requestFocus()

        handler.postDelayed({ activity?.showKeyBoard() }, 200)

        if (type == Type.DESCRIPTION) {
            tvFinish.setText(R.string.done)
            ivFinish.setImageResource(R.drawable.ic_down_stateful)
        } else {
            tvFinish.setText(R.string.next)
            ivFinish.setImageResource(R.drawable.ic_next_stateful)
            disableFinish()
        }

        tvSlogan.setText(
            when (type) {
                Type.TITLE -> R.string.create_title_for_your_behavior
                Type.DESCRIPTION -> R.string.create_description_for_your_behavior
            }
        )

        etDetail.setHint(
            when (type) {
                Type.TITLE -> R.string.enter_your_title_here
                Type.DESCRIPTION -> R.string.enter_your_optional_des_here
            }
        )

        etDetail.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty() && type == Type.TITLE) {
                disableFinish()
            } else {
                enableFinish()
            }
        }

        layoutBack.setSafetyOnclickListener {
            if(!navigator.anim(RIGHT_LEFT).popFragment()) {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
        }

        layoutFinish.setSafetyOnclickListener {
            val content = etDetail.text.toString().trim()
            when (type) {
                Type.TITLE -> {
                    data.title = content
                    navigator.anim(RIGHT_LEFT).replaceFragment(
                        R.id.layoutContainer,
                        newInstance(data, Type.DESCRIPTION),
                        true
                    )
                }
                Type.DESCRIPTION -> {
                    data.description = content
                    viewModel.addBehavior(data)
                }
            }
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.addBehaviorLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val dialog = BottomAlertDialog(
                        context!!,
                        R.string.added,
                        R.string.your_behavior_has_been_added,
                        R.string.thanks_for_taking_time_to_make_community_healthier_behavior,
                        R.string.ok
                    )
                    dialog.setOnDismissListener {
                        val data = Intent().apply {
                            val bundle = Bundle().apply {
                                putParcelable(DATA, res.data()!!)
                            }
                            putExtras(bundle)
                        }
                        navigator.anim(RIGHT_LEFT).finishActivityForResult(data)
                    }
                    dialog.show()
                    blocked = false
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.BEHAVIOR_ADDING_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_add_new_behavior)
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
    }

    private fun disableFinish() {
        layoutFinish.disable()
        ivFinish.disable()
        tvFinish.disable()
    }

    private fun enableFinish() {
        layoutFinish.enable()
        ivFinish.enable()
        tvFinish.enable()
    }

    override fun onBackPressed(): Boolean {
        return navigator.anim(RIGHT_LEFT).popFragment()
    }
}