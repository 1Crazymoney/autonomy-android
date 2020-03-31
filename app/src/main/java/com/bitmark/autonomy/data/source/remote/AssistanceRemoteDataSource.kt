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

    fun listHelpRequest() = autonomyApi.listHelpRequest().map { res ->
        // TODO change later
        val req = res["result"] ?: error("invalid response format")
        listOf(req, req, req, req, req, req, req, req, req, req, req, req)
    }.subscribeOn(Schedulers.io())

    fun respondHelpRequest(id: String) =
        autonomyApi.respondHelpRequest(id).subscribeOn(Schedulers.io())

}