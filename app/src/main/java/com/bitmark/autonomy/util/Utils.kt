/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.util

import android.os.Build

fun isAboveN() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

fun isAboveO() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O