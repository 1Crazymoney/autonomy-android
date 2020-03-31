/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote

import com.bitmark.autonomy.data.source.remote.api.middleware.RxErrorHandlingComposer
import com.bitmark.autonomy.data.source.remote.api.request.RequestHelpRequest
import com.bitmark.autonomy.data.source.remote.api.service.AutonomyApi
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class AssistanceRemoteDataSource @Inject constructor(
    autonomyApi: AutonomyApi,
    rxErrorHandlingComposer: RxErrorHandlingComposer
) : RemoteDataSource(autonomyApi, rxErrorHandlingComposer) {

    fun requestHelp(
        subject: String,
        exactNeed: String,
        meetingLocation: String,
        contactInfo: String
    ) = autonomyApi.requestHelp(
        RequestHelpRequest(
            subject,
            exactNeed,
            meetingLocation,
            contactInfo
        )
    ).subscribeOn(Schedulers.io())

}