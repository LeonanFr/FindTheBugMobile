package com.app.findthebug.di

import androidx.lifecycle.ViewModel
import com.app.findthebug.presentation.viewmodel.GameViewModel
import com.app.findthebug.presentation.viewmodel.LobbyViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {

    @Binds
    @ViewModelScoped
    abstract fun bindLobbyViewModel(lobbyViewModel: LobbyViewModel): ViewModel

    @Binds
    @ViewModelScoped
    abstract fun bindGameViewModel(gameViewModel: GameViewModel): ViewModel
}