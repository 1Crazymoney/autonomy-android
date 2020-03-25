/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.ext

import com.bitmark.autonomy.data.source.remote.api.error.HttpException
import com.bitmark.autonomy.data.source.remote.api.error.NetworkException
import com.bitmark.autonomy.data.source.remote.api.error.UnknownException
import java.io.IOException

fun Throwable.isNetworkError() = this is NetworkException

fun Throwable.isHttpError() = this is HttpException

fun Throwable.toRemoteError() = when (this) {
    is IOException -> NetworkException(this)
    is com.bitmark.apiservice.utils.error.HttpException, is retrofit2.HttpException -> {
        val code = when (this) {
            is com.bitmark.apiservice.utils.error.HttpException -> statusCode
            is retrofit2.HttpException -> code()
            else -> -1
        }
        val message = when (this) {
            is com.bitmark.apiservice.utils.error.HttpException -> "error: $errorMessage, reason: $reason"
            is retrofit2.HttpException -> response()?.errorBody()?.string() ?: message()
            else -> message ?: ""
        }
        HttpException(code, message)
    }
    else -> UnknownException(this)
}