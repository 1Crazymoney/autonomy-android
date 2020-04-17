/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import com.bitmark.autonomy.util.ext.move
import com.bitmark.autonomy.util.view.FragmentStatePagerAdapter

open class ViewPagerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var currentFragment: Fragment? = null
        private set

    protected val fragments = mutableListOf<Fragment>()

    fun set(fragments: List<Fragment>) {
        this.fragments.clear()
        this.fragments.addAll(fragments)
        notifyDataSetChanged()
    }

    override fun getTag(position: Int): String {
        return fragments[position].tag ?: super.getTag(position)
    }

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun getCount(): Int = fragments.size

    override fun setPrimaryItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        if (currentFragment != `object`) {
            currentFragment = `object` as Fragment
        }
        super.setPrimaryItem(container, position, `object`)
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    fun move(fromPos: Int, toPos: Int) {
        fragments.move(fromPos, toPos)
        notifyDataSetChanged()
    }

    fun remove(pos: Int) {
        fragments.removeAt(pos)
        notifyDataSetChanged()
    }

    fun add(index: Int, fragment: Fragment): Boolean {
        if (fragments.contains(fragment)) return false
        fragments.add(index, fragment)
        notifyDataSetChanged()
        return true
    }

}