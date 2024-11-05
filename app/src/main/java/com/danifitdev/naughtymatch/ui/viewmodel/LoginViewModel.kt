package com.danifitdev.naughtymatch.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.domain.usecases.LoginWithEmailUseCase
import com.danifitdev.naughtymatch.domain.usecases.LoginWithFacebookUseCase
import com.danifitdev.naughtymatch.domain.usecases.RegisterWithEmailUseCase
import com.danifitdev.naughtymatch.utils.AuthState
import com.danifitdev.naughtymatch.utils.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val loginWithFacebookUseCase: LoginWithFacebookUseCase,
    private val userPreferencesRepository: UserPreferencesRepository

): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setLoading(loading: Boolean){
        _isLoading.value = loading
    }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _errorMessage = MutableStateFlow<String?>("")
    val errorMessage: StateFlow<String?> get() = _errorMessage

    val isLoggedIn: StateFlow<Boolean> = userPreferencesRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _registroExitoso= MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso

    fun loginWithEmail(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = loginWithEmailUseCase(email, password)
            if (result.isSuccess) {
                _isLoading.value = false
                userPreferencesRepository.setLoggedIn(true)
                _user.value = result.getOrNull()
                _user.value?.let { userPreferencesRepository.saveUser(it) }
            } else {
                _isLoading.value = false
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun registerWithEmail(email: String, password: String) = viewModelScope.launch {
        _isLoading.value = true
        viewModelScope.launch {
            val result = registerWithEmailUseCase(email, password)
            if (result.isSuccess) {
                _isLoading.value = false
                _registroExitoso.value = true
            } else {
                _isLoading.value = false
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun loginWithFacebook(token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = loginWithFacebookUseCase(token)
            if (result.isSuccess) {
                _isLoading.value = false
                userPreferencesRepository.setLoggedIn(true)
                _user.value = result.getOrNull()
                _user.value?.let { userPreferencesRepository.saveUser(it) }
            } else {
                _isLoading.value = false
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun validarDatos(email: String, password: String){
        if (!emailPattern.matcher(email).matches()) {
            _errorMessage.value = "Por favor, ingresa un correo electrónico válido."
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres."
            return
        }
        loginWithEmail(email, password)
    }
    fun limpiarMensajeError(){
        _errorMessage.value = ""
    }

    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )

}