/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.addresource.select

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
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.addresource.add.AddResourceActivity
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.livedata.Resource
import com.bitmark.autonomy.util.modelview.ResourceModelView
import com.bitmark.autonomy.util.view.BottomProgressDialog
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.activity_select_resource.*
import java.util.*
import javax.inject.Inject


class SelectResourceActivity : BaseAppCompatActivity() {

    companion object {
        private const val ADD_RESOURCE_REQUEST_CODE = 0xAA

        private const val POI_ID = "poi_id"

        private const val SELECTED_RESOURCES = "selected_resources"

        fun getBundle(poiId: String) = Bundle().apply { putString(POI_ID, poiId) }

        fun extractBundle(data: Intent?) =
            data?.extras?.getParcelableArrayList<ResourceModelView>(SELECTED_RESOURCES)
    }

    @Inject
    internal lateinit var viewModel: SelectResourceViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private lateinit var poiId: String

    private lateinit var resources: List<ResourceModelView>

    private val handler = Handler()

    private val adapter = ResourceAdapter()

    private var blocked = false

    override fun layoutRes(): Int = R.layout.activity_select_resource

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        poiId = intent?.extras?.getString(POI_ID) ?: error("missing poi_id")
        viewModel.listImportantResource(poiId, Locale.getDefault().langCountry())
    }

    override fun initComponents() {
        super.initComponents()

        disableSubmit()

        adapter.setItemClickListener(object :
            ResourceAdapter.ItemClickListener {

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
        rvResources.layoutManager = layoutManager
        rvResources.adapter = adapter

        layoutSubmit.setSafetyOnclickListener {
            if (blocked) return@setSafetyOnclickListener
            val selectedResources = adapter.getSelectedResources()
            val newResNames = selectedResources.filter { r -> r.id == null }.map { r -> r.name }
            val existingResIds = selectedResources.filter { r -> r.id != null }.map { r -> r.id!! }
            viewModel.addResource(poiId, existingResIds, newResNames)
        }

        layoutBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }

        layoutAddOther.setSafetyOnclickListener {
            val bundle = AddResourceActivity.getBundle(poiId)
            navigator.anim(BOTTOM_UP).startActivityForResult(
                AddResourceActivity::class.java,
                ADD_RESOURCE_REQUEST_CODE,
                bundle
            )
        }
    }

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.listImportantResourceLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    resources = res.data()!!
                    adapter.set(resources)
                    if (resources.isEmpty()) {
                        adapter.addFooter()
                    }
                }

                res.isLoading() -> {
                    progressBar.visible()
                }

                res.isError() -> {
                    logger.logError(Event.RESOURCE_LISTING_ERROR, res.throwable())
                    progressBar.gone()
                }
            }
        })

        viewModel.addNewResourceLiveData.asLiveData()
            .observe(this, object : Observer<Resource<List<ResourceModelView>>> {

                lateinit var progressDialog: BottomProgressDialog

                override fun onChanged(res: Resource<List<ResourceModelView>>) {
                    when {
                        res.isSuccess() -> {
                            handler.postDelayed({
                                progressDialog.dismiss()
                                val intent = Intent().apply {
                                    val bundle = Bundle().apply {
                                        putParcelableArrayList(
                                            SELECTED_RESOURCES,
                                            ArrayList(res.data()!!)
                                        )
                                    }
                                    putExtras(bundle)
                                }
                                navigator.anim(RIGHT_LEFT).finishActivityForResult(intent)
                                blocked = false
                            }, 1000)
                        }

                        res.isLoading() -> {
                            blocked = true
                            progressDialog = BottomProgressDialog(
                                this@SelectResourceActivity,
                                R.string.submitting,
                                R.string.adding_your_resources_for_this_place
                            )
                            progressDialog.show()
                        }

                        res.isError() -> {
                            logger.logError(Event.RESOURCE_ADDING_ERROR, res.throwable())
                            handler.postDelayed({
                                progressDialog.dismiss()
                                if (connectivityHandler.isConnected()) {
                                    dialogController.alert(
                                        R.string.error,
                                        R.string.could_not_add_new_behavior
                                    )
                                } else {
                                    dialogController.showNoInternetConnection()
                                }
                                blocked = false
                            }, 1000)
                        }
                    }
                }
            })
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ADD_RESOURCE_REQUEST_CODE) {
            val newResource = AddResourceActivity.extractData(data)!!
            if (adapter.isExisting(newResource)) {
                adapter.setSelected(newResource, selected = true, selectable = false)
            } else {
                adapter.add(newResource, selected = true, selectable = false)
            }

            adapter.removeFooter()
            enableSubmit()
        }
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}