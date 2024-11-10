package com.danifitdev.naughtymatch.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.utils.UserPreferencesRepository
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    fun updateUser(user: User){
        viewModelScope.launch {
            userPreferencesRepository.saveUser(user)
        }
    }

    private val _emparejado = MutableStateFlow(false)
    val emparejado: StateFlow<Boolean> = _emparejado

    fun logout() {
        viewModelScope.launch {
            // Cierra sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // Cierra sesión en Facebook
            LoginManager.getInstance().logOut()
            userPreferencesRepository.setLoggedIn(false)
        }
    }

    suspend fun actualizarInformacion(imageUri: Uri?, user: User): Boolean {
        _isLoading.value = true
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val storageRef = FirebaseStorage.getInstance().reference.child("profilePictures/${user.id}.jpg")


        FirebaseAuth.getInstance().languageCode

        return try {
            val profileUpdates: UserProfileChangeRequest
            if(imageUri != null){
                val uploadTask = storageRef.putFile(imageUri!!)
                val downloadUri = uploadTask.await().storage.downloadUrl.await()
                profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(user.nombre)
                    .setPhotoUri(downloadUri)
                    .build()
            }else{
                profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(user.nombre)
                    .build()
            }

            firebaseUser?.updateProfile(profileUpdates)?.await()
            val user = user?.let { User(it.id, firebaseUser!!.displayName, firebaseUser.email, "", firebaseUser.photoUrl.toString(), user.fecha_nac, user.genero) }
            updateUser(user!!)
            updateUserInDatabase(user)
            _isLoading.value = false
            true
        } catch (e: Exception) {
            println("Error actualizando el perfil: ${e.message}")
            _isLoading.value = false
            false
        }
    }

    suspend fun updateUserInDatabase(user: User) {
        val database = FirebaseDatabase.getInstance().getReference("usuarios").child(user.id)
        val userMap = mapOf(
            "id" to user.id,
            "nombre" to user.nombre,
            "correo" to user.correo,
            "telefono" to user.telefono,
            "foto_perfil" to user.foto_perfil,
            "fecha_nac" to user.fecha_nac,
            "genero" to user.genero
        )
        database.updateChildren(userMap).await()
    }
}