/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.recovery.access

import android.os.Bundle
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel
import com.bitmark.autonomy.feature.Navigator
import com.bitmark.autonomy.feature.Navigator.Companion.RIGHT_LEFT
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.android.synthetic.main.fragment_recovery_access.*
import javax.inject.Inject


class RecoveryAccessFragment : BaseSupportFragment() {

    companion object {

        private const val WORDS = "words"

        fun newInstance(words: List<String>): RecoveryAccessFragment {
            val fragment = RecoveryAccessFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(WORDS, ArrayList(words))
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    internal lateinit var navigator: Navigator

    override fun layoutRes(): Int = R.layout.fragment_recovery_access

    override fun viewModel(): BaseViewModel? = null

    override fun initComponents() {
        super.initComponents()

        val words = arguments?.getStringArrayList(WORDS)
            ?: throw IllegalArgumentException("missing words value")
        tvIndicator.text = context?.getString(R.string.word_of_format)?.format(1, words.size)

        rvPhrase.setOverScrollEnabled(true)
        rvPhrase.setSlideOnFling(true)
        rvPhrase.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.0f)
                .setMinScale(0.5f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build()
        )
        rvPhrase.addOnItemChangedListener { _, index ->
            tvIndicator.text = context?.getString(R.string.word_of_format)?.format(index + 1, 13)
        }

        val adapter = RecoveryKeyAdapter()
        adapter.set(words)
        rvPhrase.adapter = adapter

        layoutBack.setOnClickListener {
            navigator.anim(RIGHT_LEFT).finishActivity()
        }
    }

    override fun onBackPressed(): Boolean {
        navigator.anim(RIGHT_LEFT).finishActivity()
        return true
    }
}