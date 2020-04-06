/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import com.bitmark.autonomy.data.source.remote.AssistanceRemoteDataSource


class AssistanceRepository(private val remoteDataSource: AssistanceRemoteDataSource) {

    fun requestHelp(
        subject: String,
        exactNeed: String,
        meetingLocation: String,
        contactInfo: String
    ) = remoteDataSource.requestHelp(subject, exactNeed, meetingLocation, contactInfo)

    fun listHelpRequest() = remoteDataSource.listHelpRequest()

    fun getHelpRequest(id: String) = remoteDataSource.getHelpRequest(id)

    fun respondHelpRequest(id: String) = remoteDataSource.respondHelpRequest(id)
}