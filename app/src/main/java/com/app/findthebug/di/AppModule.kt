package com.app.findthebug.di

import android.app.Application
import android.content.Context
import com.app.findthebug.data.repository.CaseRepositoryImpl
import com.app.findthebug.data.repository.EvidenceRepositoryImpl
import com.app.findthebug.data.repository.GameRepositoryImpl
import com.app.findthebug.data.repository.SessionRepositoryImpl
import com.app.findthebug.domain.repository.ICaseRepository
import com.app.findthebug.domain.repository.IEvidenceRepository
import com.app.findthebug.domain.repository.IGameRepository
import com.app.findthebug.domain.repository.ISessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideGameRepository(gameRepositoryImpl: GameRepositoryImpl): IGameRepository = gameRepositoryImpl

    @Provides
    @Singleton
    fun provideCaseRepository(caseRepositoryImpl: CaseRepositoryImpl): ICaseRepository = caseRepositoryImpl

    @Provides
    @Singleton
    fun provideEvidenceRepository(evidenceRepositoryImpl: EvidenceRepositoryImpl): IEvidenceRepository = evidenceRepositoryImpl

    @Provides
    @Singleton
    fun provideSessionRepository(sessionRepositoryImpl: SessionRepositoryImpl): ISessionRepository = sessionRepositoryImpl
}