/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.guidance

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseAppCompatActivity
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.DialogController
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.bitmark.autonomy.feature.connectivity.ConnectivityHandler
import com.bitmark.autonomy.logging.Event
import com.bitmark.autonomy.logging.EventLogger
import com.bitmark.autonomy.util.ext.langCountry
import com.bitmark.autonomy.util.ext.openVideoPlayer
import com.bitmark.autonomy.util.ext.setSafetyOnclickListener
import com.bitmark.autonomy.util.ext.showNoInternetConnection
import com.bitmark.autonomy.util.modelview.GuidanceModelView
import kotlinx.android.synthetic.main.activity_guidance.*
import java.util.*
import javax.inject.Inject


class GuidanceActivity : BaseAppCompatActivity() {

    companion object {
        private const val HAND_WASHING_EN_URL = "https://youtu.be/OkMJ8NYeVUE"

        private const val HAND_WASHING_TW_URL = "https://youtu.be/4_QBE_p0TqI"

        private const val HAND_SANITIZER_EN_URL = "https://youtu.be/q2hMrlnU5Xk"

        private const val HAND_SANITIZER_TW_URL = "https://youtu.be/pBGKvGxHvjk"

        private const val MASK_WEARING_EN_URL = "https://youtu.be/h7MOW7tODRs"

        private const val MASK_WEARING_TW_URL = "https://youtu.be/p5eaGJivY4U"

        private const val COUGHS_COVERING_TW = "https://youtu.be/a3RXWMN-QgE"
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    @Inject
    internal lateinit var connectivityHandler: ConnectivityHandler

    @Inject
    internal lateinit var logger: EventLogger

    override fun layoutRes(): Int = R.layout.activity_guidance

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, RecyclerView.VERTICAL)
        itemDecoration.setDrawable(getDrawable(R.drawable.bg_divider)!!)
        rv.addItemDecoration(itemDecoration)
        val adapter = GuidanceRecyclerViewAdapter()

        adapter.setItemClickListener(object : GuidanceRecyclerViewAdapter.ItemClickListener {
            override fun onPlayClick(url: String) {
                if (connectivityHandler.isConnected()) {
                    navigator.openVideoPlayer(url) { e ->
                        logger.logError(Event.VIDEO_PLAYING_ERROR, e)
                        dialogController.alert(R.string.error, R.string.could_not_play_video)
                    }
                } else {
                    dialogController.showNoInternetConnection()
                }
            }

        })

        val isZhTw = Locale.getDefault().langCountry().equals("zh-tw", true)
        val data = mutableListOf(
            GuidanceModelView(
                R.string.hand_washing,
                if (isZhTw) HAND_WASHING_TW_URL else HAND_WASHING_EN_URL
            ),
            GuidanceModelView(
                R.string.applying_hand_sanitizer,
                if (isZhTw) HAND_SANITIZER_TW_URL else HAND_SANITIZER_EN_URL
            ),
            GuidanceModelView(
                R.string.wearing_surgical_mask,
                if (isZhTw) MASK_WEARING_TW_URL else MASK_WEARING_EN_URL
            )
        )

        if (isZhTw) {
            data.add(
                GuidanceModelView(
                    R.string.covering_coughs,
                    COUGHS_COVERING_TW
                )
            )
        }
        adapter.set(data)
        rv.adapter = adapter

        layoutBack.setSafetyOnclickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}