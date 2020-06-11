/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.addresource.add

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.BOTTOM_UP
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.ResourceModelView
import kotlinx.android.synthetic.main.activity_add_resource.*
import java.util.*
import javax.inject.Inject


class AddResourceActivity : BaseAppCompatActivity() {

    companion object {

        private const val RESOURCE = "resource"

        private const val POI_ID = "poi_id"

        fun getBundle(poiId: String) = Bundle().apply {
            putString(POI_ID, poiId)
        }

        fun extractData(data: Intent?) = data?.extras?.getParcelable<ResourceModelView>(RESOURCE)
    }

    @Inject
    internal lateinit var viewModel: AddResourceViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var resources: List<ResourceModelView>

    private val adapter = AutocompleteAdapter()

    private val handler = Handler()

    override fun layoutRes(): Int = R.layout.activity_add_resource

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val poiId = intent?.extras?.getString(POI_ID) ?: error("missing poi_id")
        viewModel.listResource(poiId, Locale.getDefault().langCountry())
    }

    override fun initComponents() {
        super.initComponents()

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvAutocomplete.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.bg_divider)!!)
        rvAutocomplete.addItemDecoration(itemDecoration)
        rvAutocomplete.adapter = adapter

        adapter.setItemClickListener { resource ->
            hideKeyBoard()
            finishWithResult(resource)
        }

        edtName.doOnTextChanged { text, _, _, _ ->
            if (!isResourceReady() || text.toString().isEmpty()) return@doOnTextChanged
            viewModel.search(resources, text.toString())
        }

        edtName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyBoard()
                val name = edtName.text.toString()
                if (name.isEmpty()) {
                    false
                } else {
                    finishWithResult(ResourceModelView(null, name.trim()))
                    true
                }
            } else false
        }

        ivExit.setSafetyOnclickListener {
            navigator.anim(BOTTOM_UP).finishActivity()
        }

        edtName.requestFocus()
        handler.postDelayed({
            showKeyBoard()
        }, 250)

    }

    private fun isResourceReady() = ::resources.isInitialized

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.searchResourceLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val p = res.data()!!
                    val result = p.first
                    val searchText = p.second
                    adapter.set(result, searchText)
                }

                res.isError() -> {
                    logger.logError(Event.RESOURCE_AUTOCOMPLETE_SEARCHING_ERROR, res.throwable())
                }
            }
        })

        viewModel.listResourceLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    resources = res.data()!!
                }

                res.isError() -> {
                    logger.logError(Event.RESOURCE_AUTOCOMPLETE_LISTING_ERROR, res.throwable())
                    if (!isResourceReady()) {
                        dialogController.unexpectedAlert {
                            navigator.anim(Navigator.RIGHT_LEFT).finishActivity()
                        }
                    }
                }
            }
        })
    }

    private fun finishWithResult(resource: ResourceModelView) {
        val intent = Intent().apply {
            putExtra(RESOURCE, resource)
        }
        navigator.anim(BOTTOM_UP).finishActivityForResult(intent, Activity.RESULT_OK)
    }

    override fun onBackPressed() {
        navigator.anim(BOTTOM_UP).finishActivity()
        super.onBackPressed()
    }
}