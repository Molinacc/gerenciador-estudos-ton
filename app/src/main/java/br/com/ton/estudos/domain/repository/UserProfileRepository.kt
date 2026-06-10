package br.com.ton.estudos.domain.repository

import br.com.ton.estudos.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
    suspend fun getUserProfileOnce(): UserProfile
    suspend fun saveUserProfile(profile: UserProfile)
}
