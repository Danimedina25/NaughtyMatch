package com.danifitdev.naughtymatch.ui.viewmodel

import android.content.Context
import android.provider.Settings
import android.util.Log
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val loginWithFacebookUseCase: LoginWithFacebookUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val appContext: Context
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
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _registroExitoso= MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso

    private val _firebaseInstance = FirebaseDatabase.getInstance()

    private val _androidId = MutableStateFlow("")
    val androidId: StateFlow<String> = _androidId

    fun getAndroidId(){
        _androidId.value = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun saveUser(user: User){
        viewModelScope.launch {
            userPreferencesRepository.saveUser(user)
        }
    }

    fun loginWithEmail(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = loginWithEmailUseCase(email, password, androidId.value)
            if (result.isSuccess) {
                _user.value = result.getOrNull()
                if(_user.value!!.androidId != androidId.value){
                    updateAndroidIdInFirebase(_user.value!!.androidId, _androidId.value)
                    _user.value!!.androidId = androidId.value
                }
                _user.value?.let { saveUser(it) }
                userPreferencesRepository.setLoggedIn(true)
                _isLoading.value = false
            } else {
                _isLoading.value = false
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun updateAndroidIdInFirebase(currentAndroidId: String, newAndroidId: String){
        _isLoading.value = true
        val databaseRef = _firebaseInstance.reference.child("usuarios")
        databaseRef.orderByChild("androidId").equalTo(currentAndroidId)
            .get()
            .addOnSuccessListener { snapshot->
                for (childSnapshot in snapshot.children) {
                    val userKey = childSnapshot.key
                    userKey?.let {
                        val updates = mapOf<String, Any>(
                            "androidId" to newAndroidId
                        )
                        databaseRef.child(it).updateChildren(updates)
                            .addOnSuccessListener {
                                _isLoading.value = false
                            }
                            .addOnFailureListener { exception ->
                                _isLoading.value = false
                               _errorMessage.value = "Error al actualizar el campo: ${exception.message}"
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error al buscar el registro: ${exception.message}"
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
                _user.value?.let { saveUser(it) }
            } else {
                _isLoading.value = false
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun validarDatos(email: String, password: String): Boolean{
        if (!emailPattern.matcher(email).matches()) {
            _errorMessage.value = "Por favor, ingresa un correo electrónico válido."
            return false
        }
        if (password.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres."
            return false
        }
        return true
    }

    fun limpiarMensajeError(){
        _errorMessage.value = ""
    }

    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )

    fun validarSiEstaRegistrado(onNavigateToLogin: () -> Unit, onNavigateToMain: () -> Unit){
        _isLoading.value = true
        val database = _firebaseInstance.reference
        database.child("usuarios")
            .orderByChild("androidId")
            .equalTo(_androidId.value)
            .get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.exists()){
                    _isLoading.value = false
                    if(isLoggedIn.value){
                        onNavigateToMain()
                    }else{
                        onNavigateToLogin()
                    }
                }else{
                    saveUser(User())
                    onNavigateToMain()
                }
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.message
                _isLoading.value = false
                onNavigateToMain()
                exception.printStackTrace()
            }
    }

}