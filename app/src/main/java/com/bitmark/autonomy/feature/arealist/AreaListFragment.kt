/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.feature.arealist

import com.bitmark.autonomy.R
import com.bitmark.autonomy.feature.BaseSupportFragment
import com.bitmark.autonomy.feature.BaseViewModel


class AreaListFragment : BaseSupportFragment() {

    companion object {
        fun newInstance() = AreaListFragment()
    }

    override fun viewModel(): BaseViewModel? = null

    override fun layoutRes(): Int = R.layout.fragment_area_list
}