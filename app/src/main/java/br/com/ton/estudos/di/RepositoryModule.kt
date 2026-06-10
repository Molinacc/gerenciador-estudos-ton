package br.com.ton.estudos.di

import br.com.ton.estudos.data.repository.*
import br.com.ton.estudos.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository
    @Binds @Singleton abstract fun bindStudySessionRepository(impl: StudySessionRepositoryImpl): StudySessionRepository
    @Binds @Singleton abstract fun bindTimerSessionRepository(impl: TimerSessionRepositoryImpl): TimerSessionRepository
    @Binds @Singleton abstract fun bindFlashcardRepository(impl: FlashcardRepositoryImpl): FlashcardRepository
    @Binds @Singleton abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository
}
