/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Observer
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.feature.Navigator.Companion.NONE
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.behavior.add2.BehaviorAdding2Activity
import com.bitmark.autonomy.feature.behavior.metric.BehaviorMetricActivity
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.livedata.Resource
import com.bitmark.autonomy.util.view.BottomProgressDialog
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_behavior_report.*
import java.util.*
import javax.inject.Inject


class BehaviorReportActivity : BaseAppCompatActivity() {

    companion object {
        private const val ADD_BEHAVIOR_REQUEST_CODE = 0x08
    }

    @Inject
    internal lateinit var viewModel: BehaviorReportViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private var blocked = false

    private val adapter = BehaviorReportRecyclerViewAdapter()

    private val handler = Handler()

    override fun layoutRes(): Int = R.layout.activity_behavior_report

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.listBehavior(Locale.getDefault().langCountry())
    }

    override fun initComponents() {
        super.initComponents()

        disableSubmit()

        adapter.setItemClickListener(object :
            BehaviorReportRecyclerViewAdapter.ItemClickListener {

            override fun onSelected() {
                enableSubmit()
            }

            override fun onDeselected() {
                disableSubmit()
            }
        })

        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.flexWrap = FlexWrap.WRAP
        rvBehaviors.layoutManager = layoutManager
        rvBehaviors.adapter = adapter

        layoutSubmit.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.reportBehaviors(adapter.getSelectedBehaviors().map { it!!.id })
        }

        layoutBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutReportOther.setSafetyOnclickListener {
            navigator.anim(BOTTOM_UP).startActivityForResult(
                BehaviorAdding2Activity::class.java,
                ADD_BEHAVIOR_REQUEST_CODE
            )
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    private fun enableSubmit() {
        layoutSubmit.enable()
        tvSubmit.enable()
        ivSubmit.enable()
    }

    private fun disableSubmit() {
        layoutSubmit.disable()
        tvSubmit.disable()
        ivSubmit.disable()
    }

    override fun observe() {
        super.observe()

        viewModel.listBehaviorLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data()!!
                    adapter.set(data)
                    if (!adapter.hasNeighborhoodBehaviors()) {
                        adapter.addFooter()
                    }
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.BEHAVIOR_LISTING_ERROR, res.throwable())
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.reportBehaviorsLiveData.asLiveData()
            .observe(this, object : Observer<Resource<Any>> {

                lateinit var progressDialog: BottomProgressDialog

                override fun onChanged(res: Resource<Any>) {
                    when {
                        res.isSuccess() -> {
                            handler.postDelayed({
                                blocked = false
                                progressDialog.dismiss()
                                navigator.anim(BOTTOM_UP)
                                    .startActivity(BehaviorMetricActivity::class.java)
                                navigator.anim(NONE)
                                    .finishActivityForResult(resultCode = Activity.RESULT_OK)
                            }, 1000)
                        }

                        res.isError() -> {
                            logger.logError(Event.BEHAVIOR_REPORT_ERROR, res.throwable())
                            handler.postDelayed({
                                progressDialog.dismiss()
                                if (connectivityHandler.isConnected()) {
                                    dialogController.alert(
                                        R.string.error,
                                        R.string.could_not_report_behaviors
                                    )
                                } else {
                                    dialogController.showNoInternetConnection()
                                }
                                blocked = false
                            }, 1000)
                        }

                        res.isLoading() -> {
                            blocked = true
                            progressDialog = BottomProgressDialog(
                                this@BehaviorReportActivity,
                                R.string.submitting,
                                R.string.reporting_your_healthy_behaviors
                            )
                            progressDialog.show()
                        }
                    }
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_BEHAVIOR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val newBehavior = BehaviorAdding2Activity.extractData(data)!!
            if (adapter.isExisting(newBehavior.id)) {
                adapter.setSelected(newBehavior.id, selected = true, selectable = false)
            } else {
                adapter.add(newBehavior, selected = true, selectable = false)
            }

            if (adapter.hasNeighborhoodBehaviors()) {
                adapter.removeFooter()
            }
            enableSubmit()
        }
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}