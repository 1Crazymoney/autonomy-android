/**
 * SPDX-License-Identifier: ISC
 * Copyright © 2014-2019 Bitmark. All rights reserved.
 * Use of this source code is governed by an ISC
 * license that can be found in the LICENSE file.
 */
package com.bitmark.autonomy.data.source

import android.content.Context
import com.bitmark.autonomy.data.source.local.*
import com.bitmark.autonomy.data.source.local.api.DatabaseGateway
import com.bitmark.autonomy.data.source.remote.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideAccountRepo(
        remoteDataSource: AccountRemoteDataSource,
        localDataSource: AccountLocalDataSource
    ): AccountRepository {
        return AccountRepository(remoteDataSource, localDataSource)
    }

    @Singleton
    @Provides
    fun provideAppRepo(
        remoteDataSource: AppRemoteDataSource,
        localDataSource: AppLocalDataSource
    ) = AppRepository(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideAssistanceRepo(
        remoteDataSource: AssistanceRemoteDataSource
    ) = AssistanceRepository(remoteDataSource)

    @Singleton
    @Provides
    fun provideSymptomRepo(
        remoteDataSource: SymptomRemoteDataSource,
        localDataSource: SymptomLocalDataSource
    ) = SymptomRepository(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideUserRepo(
        remoteDataSource: UserRemoteDataSource
    ) = UserRepository(remoteDataSource)

    @Singleton
    @Provides
    fun provideBehaviorRepo(
        remoteDataSource: BehaviorRemoteDataSource,
        localDataSource: BehaviorLocalDataSource
    ) = BehaviorRepository(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideResourceRepo(
        remoteDataSource: ResourceRemoteDataSource,
        localDataSource: ResourceLocalDataSource
    ) = ResourceRepository(remoteDataSource, localDataSource)

    @Singleton
    @Provides
    fun provideDatabaseGateway(context: Context): DatabaseGateway {
        return object : DatabaseGateway() {}
    }
}