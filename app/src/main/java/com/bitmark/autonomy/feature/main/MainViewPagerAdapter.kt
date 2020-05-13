/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.main

import androidx.fragment.app.FragmentManager
import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.ViewPagerAdapter
import com.bitmark.autonomy.util.view.pagerindicator.IconPagerAdapter


class MainViewPagerAdapter(fm: FragmentManager) : ViewPagerAdapter(fm), IconPagerAdapter {

    override fun getIconResId(index: Int): Int {
        return when (index) {
            0 -> R.drawable.ic_navigation_stateful
            fragments.size - 1 -> R.drawable.ic_plus_stateful
            else -> R.drawable.ic_round_stateful
        }
    }

    fun indexOfAreaFragment(id: String) =
        fragments.indexOfFirst { f -> (f as? MainFragment)?.getAreaId() == id }

    fun updateAreaAlias(id: String, alias: String): Boolean {
        val index = indexOfAreaFragment(id)
        return if (index != -1) {
            (fragments[index] as MainFragment).updateAlias(alias)
            true
        } else {
            false
        }
    }

    fun closeViewSourcePanels() {
        fragments.forEach { f -> (f as? MainFragment)?.closeViewSourcePanel() }
    }
}