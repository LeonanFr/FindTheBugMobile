package com.app.findthebug.di

import com.app.findthebug.data.remote.api.ApiClient
import com.app.findthebug.data.remote.api.WebSocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiClient(): ApiClient = ApiClient

    @Provides
    @Singleton
    fun provideWebSocketService(): WebSocketService = WebSocketService()
}