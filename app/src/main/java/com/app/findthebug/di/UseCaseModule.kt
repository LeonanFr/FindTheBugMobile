package com.app.findthebug.di

import com.app.findthebug.domain.usecase.cases.GetCaseDetailsUseCase
import com.app.findthebug.domain.usecase.cases.GetCasesUseCase
import com.app.findthebug.domain.usecase.evidence.GetEvidencesUseCase
import com.app.findthebug.domain.usecase.evidence.SaveEvidenceUseCase
import com.app.findthebug.domain.usecase.game.*
import com.app.findthebug.domain.usecase.session.LoadSessionUseCase
import com.app.findthebug.domain.usecase.session.SaveSessionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideCreateLobbyUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): CreateLobbyUseCase {
        return CreateLobbyUseCase(gameRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideJoinLobbyUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): JoinLobbyUseCase {
        return JoinLobbyUseCase(gameRepository)
    }


    @Provides
    @ViewModelScoped
    fun provideStartGameUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): StartGameUseCase {
        return StartGameUseCase(gameRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideExecuteActionUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): ExecuteActionUseCase {
        return ExecuteActionUseCase(gameRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSubmitSolutionUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): SubmitSolutionUseCase {
        return SubmitSolutionUseCase(gameRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSaveNoteUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): SaveNoteUseCase {
        return SaveNoteUseCase(gameRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideValidateSolutionUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): ValidateSolutionUseCase {
        return ValidateSolutionUseCase(gameRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetGameStateUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): GetGameStateUseCase {
        return GetGameStateUseCase(gameRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetLobbyInfoUseCase(gameRepository: com.app.findthebug.domain.repository.IGameRepository): GetLobbyInfoUseCase {
        return GetLobbyInfoUseCase(gameRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetCasesUseCase(caseRepository: com.app.findthebug.domain.repository.ICaseRepository): GetCasesUseCase {
        return GetCasesUseCase(caseRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetCaseDetailsUseCase(caseRepository: com.app.findthebug.domain.repository.ICaseRepository): GetCaseDetailsUseCase {
        return GetCaseDetailsUseCase(caseRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSaveSessionUseCase(sessionRepository: com.app.findthebug.domain.repository.ISessionRepository): SaveSessionUseCase {
        return SaveSessionUseCase(sessionRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideLoadSessionUseCase(sessionRepository: com.app.findthebug.domain.repository.ISessionRepository): LoadSessionUseCase {
        return LoadSessionUseCase(sessionRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSaveEvidenceUseCase(evidenceRepository: com.app.findthebug.domain.repository.IEvidenceRepository): SaveEvidenceUseCase {
        return SaveEvidenceUseCase(evidenceRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetEvidencesUseCase(evidenceRepository: com.app.findthebug.domain.repository.IEvidenceRepository): GetEvidencesUseCase {
        return GetEvidencesUseCase(evidenceRepository)
    }
}