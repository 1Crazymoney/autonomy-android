/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util.modelview

import androidx.annotation.StringRes


data class GuidanceModelView(
    @StringRes val title: Int,

    val videoUrl: String
)

val GuidanceModelView.previewUrl: String
    get() = "https://img.youtube.com/vi/${videoUrl.replace("https://youtu.be/", "")}/hqdefault.jpg"