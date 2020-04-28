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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.feature.symptoms.add.SymptomAddingContainerActivity
import com.bitmark.autonomy.feature.symptoms.add.SymptomAddingFragment
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.view.BottomAlertDialog
import kotlinx.android.synthetic.main.activity_symptoms_report.*
import javax.inject.Inject


class SymptomReportActivity : BaseAppCompatActivity() {

    companion object {
        private const val ADD_SYMPTOM_REQUEST_CODE = 0x07
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

    override fun layoutRes(): Int = R.layout.activity_symptoms_report

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.listSymptom()
    }

    override fun initComponents() {
        super.initComponents()

        disableSubmit()

        adapter.setItemClickListener(object :
            SymptomRecyclerViewAdapter.ItemClickListener {

            override fun onAddNew() {
                navigator.anim(RIGHT_LEFT).startActivityForResult(
                    SymptomAddingContainerActivity::class.java,
                    ADD_SYMPTOM_REQUEST_CODE
                )
            }

            override fun onChecked() {
                enableSubmit()
            }

            override fun onUnChecked() {
                disableSubmit()
            }
        })

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvSymptoms.layoutManager = layoutManager
        rvSymptoms.adapter = adapter

        layoutSubmit.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            viewModel.reportSymptoms(adapter.getCheckedSymptoms().map { it!!.id })
        }

        layoutBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
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
                    adapter.set(res.data()!!)
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

        viewModel.reportSymptomLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val dialog = BottomAlertDialog(
                        this,
                        R.string.reported,
                        R.string.your_symptoms_have_been_reported,
                        R.string.thanks_for_taking_the_time_symptoms,
                        R.string.ok
                    )
                    dialog.setOnDismissListener {
                        navigator.anim(RIGHT_LEFT)
                            .finishActivityForResult(resultCode = Activity.RESULT_OK)
                    }
                    dialog.show()
                    blocked = false
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.SYMPTOM_REPORT_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_report_symptoms)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_SYMPTOM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val newSymptom = SymptomAddingFragment.extractResultData(data)!!
            adapter.add(newSymptom, checked = true, checkable = false)
        }
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}