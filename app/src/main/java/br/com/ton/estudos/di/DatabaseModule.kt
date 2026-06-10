package br.com.ton.estudos.di

import android.content.Context
import androidx.room.Room
import br.com.ton.estudos.data.local.database.StudosDatabase
import br.com.ton.estudos.data.local.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudosDatabase =
        Room.databaseBuilder(context, StudosDatabase::class.java, "estudos_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSubjectDao(db: StudosDatabase): SubjectDao = db.subjectDao()
    @Provides fun provideStudySessionDao(db: StudosDatabase): StudySessionDao = db.studySessionDao()
    @Provides fun provideTimerSessionDao(db: StudosDatabase): TimerSessionDao = db.timerSessionDao()
    @Provides fun provideFlashcardDeckDao(db: StudosDatabase): FlashcardDeckDao = db.flashcardDeckDao()
    @Provides fun provideFlashcardDao(db: StudosDatabase): FlashcardDao = db.flashcardDao()
    @Provides fun provideUserProfileDao(db: StudosDatabase): UserProfileDao = db.userProfileDao()
}
