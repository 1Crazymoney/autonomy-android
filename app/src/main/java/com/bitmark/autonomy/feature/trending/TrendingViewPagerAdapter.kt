/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.trending

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.ViewPagerAdapter

class TrendingViewPagerAdapter(private val context: Context, fm: FragmentManager) :
    ViewPagerAdapter(fm) {

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(
            when (position) {
                Period.WEEK -> R.string.week
                Period.MONTH -> R.string.month
                Period.YEAR -> R.string.year
                else -> throw IllegalArgumentException("invalid tab pos")
            }
        )
    }
}