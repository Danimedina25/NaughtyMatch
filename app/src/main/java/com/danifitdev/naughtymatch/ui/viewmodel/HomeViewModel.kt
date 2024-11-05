package com.danifitdev.naughtymatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.utils.UserPreferencesRepository
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setLoading(loading: Boolean){
        _isLoading.value = loading
    }

    val isLoggedIn: StateFlow<Boolean> = userPreferencesRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val currentUser: StateFlow<User?> = userPreferencesRepository.user
        .stateIn(viewModelScope, SharingStarted.Lazily, User())


    fun logout() {
        viewModelScope.launch {
            // Cierra sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // Cierra sesión en Facebook
            LoginManager.getInstance().logOut()
            userPreferencesRepository.setLoggedIn(false)
        }
    }
}