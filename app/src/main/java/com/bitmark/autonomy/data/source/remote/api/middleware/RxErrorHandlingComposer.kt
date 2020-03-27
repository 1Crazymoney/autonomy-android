/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2020 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source.remote.api.middleware

import com.bitmark.autonomy.data.ext.toRemoteError
import io.reactivex.*
import javax.inject.Inject
import io.reactivex.functions.Function

class RxErrorHandlingComposer @Inject constructor() {

    fun <T> single(publisher: SingleOnSubscribe<T>): Single<T> =
        Single.create(publisher).onErrorResumeNext { e -> Single.error(e.toRemoteError()) }

    fun completable(publisher: CompletableOnSubscribe) =
        Completable.create(publisher).onErrorResumeNext { e ->
            Completable.error(
                e.toRemoteError()
            )
        }

    fun <T> maybe(publisher: MaybeOnSubscribe<T>) =
        Maybe.create(publisher).onErrorResumeNext(
            Function { e -> Maybe.error(e.toRemoteError()) })

    fun <T> observable(publisher: ObservableOnSubscribe<T>) =
        Observable.create(publisher).onErrorResumeNext(Function { e ->
            Observable.error(
                e.toRemoteError()
            )
        })

    fun <T> flowable(
        publisher: FlowableOnSubscribe<T>,
        backpressureStrategy: BackpressureStrategy
    ) =
        Flowable.create(publisher, backpressureStrategy).onErrorResumeNext(
            Function { e -> Flowable.error(e.toRemoteError()) })
}