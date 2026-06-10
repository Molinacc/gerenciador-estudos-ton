package br.com.ton.estudos.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ton.estudos.domain.model.UserProfile
import br.com.ton.estudos.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            profileRepo.getUserProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        userProfile = profile,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            profileRepo.saveUserProfile(profile)
        }
    }
}
