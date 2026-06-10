package br.com.ton.estudos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.ton.estudos.data.local.database.dao.*
import br.com.ton.estudos.data.local.database.entity.*

@Database(
    entities = [
        SubjectEntity::class,
        StudySessionEntity::class,
        TimerSessionEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudosDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun flashcardDeckDao(): FlashcardDeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun userProfileDao(): UserProfileDao
}
