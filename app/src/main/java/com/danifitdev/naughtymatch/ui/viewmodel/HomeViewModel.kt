package com.danifitdev.naughtymatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> get() = _currentUser

    val isLoggedIn: StateFlow<Boolean> = userPreferencesRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun refreshUser() {
        _currentUser.value = auth.currentUser
    }

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