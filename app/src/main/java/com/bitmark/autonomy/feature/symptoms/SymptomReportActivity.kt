/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.symptoms

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
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.symptoms.add2.SymptomAdding2Activity
import com.bitmark.autonomy.feature.symptoms.metric.SymptomMetricActivity
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.livedata.Resource
import com.bitmark.autonomy.util.view.BottomProgressDialog
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_symptoms_report.*
import java.util.*
import javax.inject.Inject


class SymptomReportActivity : BaseAppCompatActivity() {

    companion object {
        private const val ADD_SYMPTOM_REQUEST_CODE = 0x07

        private const val SELECTED_SYMPTOMS = "selected_symptoms"

        fun getBundle(selectedSymptoms: ArrayList<String>? = null) = Bundle().apply {
            if (selectedSymptoms != null) putStringArrayList(SELECTED_SYMPTOMS, selectedSymptoms)
        }
    }

    @Inject
    internal lateinit var viewModel: SymptomReportViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private var blocked = false

    private val adapter = SymptomRecyclerViewAdapter()

    private val handler = Handler()

    override fun layoutRes(): Int = R.layout.activity_symptoms_report

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.listSymptom(Locale.getDefault().langCountry())
    }

    override fun initComponents() {
        super.initComponents()

        disableSubmit()

        adapter.setItemClickListener(object :
            SymptomRecyclerViewAdapter.ItemClickListener {

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
        rvSymptoms.layoutManager = layoutManager
        rvSymptoms.adapter = adapter

        layoutSubmit.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.reportSymptoms(adapter.getSelectedSymptoms().map { it!!.id })
        }

        layoutBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutReportOther.setSafetyOnclickListener {
            navigator.anim(BOTTOM_UP).startActivityForResult(
                SymptomAdding2Activity::class.java,
                ADD_SYMPTOM_REQUEST_CODE
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

        viewModel.listSymptomLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val data = res.data()!!
                    adapter.set(data)
                    if (!adapter.hasNeighborhoodSymptom()) {
                        adapter.addFooter()
                    }

                    val selectedSymptoms =
                        intent?.extras?.getStringArrayList(SELECTED_SYMPTOMS) ?: return@Observer
                    adapter.setSelected(selectedSymptoms)
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.SYMPTOM_LISTING_ERROR, res.throwable())
                }

                res.isLoading() -> {
                    progressBar.visible()
                }
            }
        })

        viewModel.reportSymptomLiveData.asLiveData()
            .observe(this, object : Observer<Resource<Any>> {

                lateinit var progressDialog: BottomProgressDialog

                override fun onChanged(res: Resource<Any>) {
                    when {
                        res.isSuccess() -> {
                            handler.postDelayed({
                                blocked = false
                                progressDialog.dismiss()
                                navigator.anim(BOTTOM_UP)
                                    .startActivity(SymptomMetricActivity::class.java)
                                navigator.anim(NONE)
                                    .finishActivityForResult(resultCode = Activity.RESULT_OK)
                            }, 1000)
                        }

                        res.isError() -> {
                            logger.logError(Event.SYMPTOM_REPORT_ERROR, res.throwable())
                            handler.postDelayed({
                                progressDialog.dismiss()
                                if (connectivityHandler.isConnected()) {
                                    dialogController.alert(
                                        R.string.error,
                                        R.string.could_not_report_symptoms
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
                                this@SymptomReportActivity,
                                R.string.submitting,
                                R.string.reporting_your_symptoms_to_your_neighborhood
                            )
                            progressDialog.show()
                        }
                    }
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_SYMPTOM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val newSymptom = SymptomAdding2Activity.extractData(data)!!
            if (adapter.isExisting(newSymptom.id)) {
                adapter.setSelected(newSymptom.id, selected = true, selectable = false)
            } else {
                adapter.add(newSymptom, selected = true, selectable = false)
            }

            if (adapter.hasNeighborhoodSymptom()) {
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