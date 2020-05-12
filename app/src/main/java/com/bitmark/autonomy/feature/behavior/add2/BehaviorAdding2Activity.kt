/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.behavior.add2

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
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.*
import com.bitmark.autonomy.util.modelview.BehaviorModelView
import kotlinx.android.synthetic.main.activity_behavior_adding_2.*
import java.util.*
import javax.inject.Inject


class BehaviorAdding2Activity : BaseAppCompatActivity() {

    companion object {

        private const val BEHAVIOR_DATA = "behavior_data"

        fun extractData(intent: Intent?) =
            intent?.getParcelableExtra<BehaviorModelView>(BEHAVIOR_DATA)
    }

    @Inject
    internal lateinit var viewModel: BehaviorAdding2ViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var logger: EventLogger

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    private lateinit var behaviors: List<BehaviorModelView>

    private val adapter = AutocompleteRecyclerViewAdapter()

    private val handler = Handler()

    override fun layoutRes(): Int = R.layout.activity_behavior_adding_2

    override fun viewModel(): BaseViewModel? = viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.listBehavior(Locale.getDefault().langCountry())
    }

    override fun initComponents() {
        super.initComponents()

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvAutocomplete.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.bg_divider)!!)
        rvAutocomplete.addItemDecoration(itemDecoration)
        rvAutocomplete.adapter = adapter

        adapter.setItemClickListener { behavior ->
            hideKeyBoard()
            val intent = Intent().apply {
                putExtra(BEHAVIOR_DATA, behavior)
            }
            navigator.anim(BOTTOM_UP).finishActivityForResult(intent, Activity.RESULT_OK)
        }

        edtName.doOnTextChanged { text, _, _, _ ->
            if (!isBehaviorReady() || text.toString().isEmpty()) return@doOnTextChanged
            viewModel.search(behaviors, text.toString())
        }

        edtName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyBoard()
                val name = edtName.text.toString()
                if (name.isEmpty()) {
                    false
                } else {
                    viewModel.addBehavior(name)
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

    override fun deinitComponents() {
        handler.removeCallbacksAndMessages(null)
        super.deinitComponents()
    }

    override fun observe() {
        super.observe()

        viewModel.listBehaviorLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    behaviors = res.data() ?: return@Observer
                }

                res.isError() -> {
                    logger.logError(Event.BEHAVIOR_AUTOCOMPLETE_LISTING_ERROR, res.throwable())
                    if (!isBehaviorReady()) {
                        dialogController.unexpectedAlert {
                            navigator.anim(RIGHT_LEFT).finishActivity()
                        }
                    }
                }
            }
        })

        viewModel.searchBehaviorLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    val p = res.data()!!
                    val result = p.first
                    val searchText = p.second
                    adapter.set(result, searchText)
                }

                res.isError() -> {
                    logger.logError(Event.BEHAVIOR_AUTOCOMPLETE_SEARCHING_ERROR, res.throwable())
                }
            }
        })

        viewModel.addNewBehaviorLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    progressBar.gone()
                    val intent = Intent().apply {
                        putExtra(BEHAVIOR_DATA, res.data()!!)
                    }
                    navigator.anim(BOTTOM_UP).finishActivityForResult(intent, Activity.RESULT_OK)
                }

                res.isLoading() -> {
                    progressBar.visible()
                }

                res.isError() -> {
                    progressBar.gone()
                    logger.logError(Event.BEHAVIOR_ADDING_ERROR, res.throwable())
                    if (connectivityHandler.isConnected()) {
                        dialogController.alert(R.string.error, R.string.could_not_add_new_behavior)
                    } else {
                        dialogController.showNoInternetConnection()
                    }
                }
            }
        })
    }

    private fun isBehaviorReady() = ::behaviors.isInitialized

    override fun onBackPressed() {
        navigator.anim(BOTTOM_UP).finishActivity()
        super.onBackPressed()
    }
}