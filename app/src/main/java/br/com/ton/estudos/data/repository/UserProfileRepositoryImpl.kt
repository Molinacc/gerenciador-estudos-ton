package br.com.ton.estudos.data.repository

import br.com.ton.estudos.data.local.database.dao.UserProfileDao
import br.com.ton.estudos.data.local.database.entity.UserProfileEntity
import br.com.ton.estudos.domain.model.UserProfile
import br.com.ton.estudos.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao
) : UserProfileRepository {
    override fun getUserProfile(): Flow<UserProfile> =
        dao.getUserProfile().map { it?.toDomain() ?: UserProfile() }

    override suspend fun getUserProfileOnce(): UserProfile =
        dao.getUserProfileOnce()?.toDomain() ?: UserProfile()

    override suspend fun saveUserProfile(profile: UserProfile) =
        dao.insertOrUpdate(profile.toEntity())

    private fun UserProfileEntity.toDomain() = UserProfile(
        id = id,
        name = name,
        photoPath = photoPath,
        dailyGoalMinutes = dailyGoalMinutes,
        weeklyGoalMinutes = weeklyGoalMinutes,
        notificationsEnabled = notificationsEnabled,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        pomodoroFocusMinutes = pomodoroFocusMinutes,
        pomodoroBreakMinutes = pomodoroBreakMinutes,
        totalStudyStreak = totalStudyStreak,
        lastStudyDate = lastStudyDate
    )
    private fun UserProfile.toEntity() = UserProfileEntity(
        id = id,
        name = name,
        photoPath = photoPath,
        dailyGoalMinutes = dailyGoalMinutes,
        weeklyGoalMinutes = weeklyGoalMinutes,
        notificationsEnabled = notificationsEnabled,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
        pomodoroFocusMinutes = pomodoroFocusMinutes,
        pomodoroBreakMinutes = pomodoroBreakMinutes,
        totalStudyStreak = totalStudyStreak,
        lastStudyDate = lastStudyDate
    )
}
