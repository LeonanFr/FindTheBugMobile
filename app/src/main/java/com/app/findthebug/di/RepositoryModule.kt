package com.app.findthebug.di

import com.app.findthebug.data.remote.api.ApiClient
import com.app.findthebug.data.remote.api.WebSocketService
import com.app.findthebug.data.repository.CaseRepositoryImpl
import com.app.findthebug.data.repository.EvidenceRepositoryImpl
import com.app.findthebug.data.repository.GameRepositoryImpl
import com.app.findthebug.data.repository.SessionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideGameRepositoryImpl(webSocketService: WebSocketService): GameRepositoryImpl {
        return GameRepositoryImpl(webSocketService)
    }

    @Provides
    @Singleton
    fun provideCaseRepositoryImpl(apiClient: ApiClient): CaseRepositoryImpl {
        return CaseRepositoryImpl(apiClient)
    }

    @Provides
    @Singleton
    fun provideEvidenceRepositoryImpl(): EvidenceRepositoryImpl {
        return EvidenceRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideSessionRepositoryImpl(): SessionRepositoryImpl {
        return SessionRepositoryImpl()
    }
}