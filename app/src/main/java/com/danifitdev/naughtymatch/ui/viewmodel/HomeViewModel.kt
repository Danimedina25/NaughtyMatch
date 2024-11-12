package com.danifitdev.naughtymatch.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.utils.UserPreferencesRepository
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
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

    private val _codigoGenerado = MutableStateFlow("")
    val codigoGenerado: StateFlow<String> = _codigoGenerado

    val isLoggedIn: StateFlow<Boolean> = userPreferencesRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val currentUser: StateFlow<User?> = userPreferencesRepository.user
        .stateIn(viewModelScope, SharingStarted.Lazily, User())

    private val _opcionGenerarCodigo = MutableStateFlow(false)
    val opcionGenerarCodigo: StateFlow<Boolean> = _opcionGenerarCodigo

    private val _opcionIngresarCodigo = MutableStateFlow(false)
    val opcionIngresarCodigo: StateFlow<Boolean> = _opcionIngresarCodigo


    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _messageLoader = MutableStateFlow<String?>("Cargando...")
    val messageLoader: StateFlow<String?> get() = _messageLoader

    private val _pareja = MutableStateFlow(User())
    val pareja: StateFlow<User> = _pareja


    fun mostrarOpcionGenerarCodigo(){
        _opcionGenerarCodigo.value = !_opcionGenerarCodigo.value
        _opcionIngresarCodigo.value = false
    }

    fun mostrarOpcionIngresarCodigo(){
        _opcionIngresarCodigo.value = !_opcionIngresarCodigo.value
        _opcionGenerarCodigo.value = false
    }

    fun updateUser(user: User){
        viewModelScope.launch {
            userPreferencesRepository.saveUser(user)
        }
    }

    fun setMessageLoader(message: String){
        _messageLoader.value = message
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

    fun generateRandomCode(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun saveMatchCode(userId: String, code: String) {
        val db = FirebaseFirestore.getInstance()
        val matchData = hashMapOf(
            "user_id" to userId,
            "status" to "pending",
            "matched_with" to null
        )

        db.collection("matches").document(code)
            .set(matchData)
            .addOnSuccessListener {
                _isLoading.value = false
                _codigoGenerado.value = code
            }
            .addOnFailureListener { exception ->
                    _isLoading.value = false
                    _errorMessage.value = "Error al generar el código: $exception"
            }
    }


    fun generateUniqueCode(length: Int) {
        _isLoading.value = true
        val db = FirebaseFirestore.getInstance()
        val newCode = generateRandomCode(length)

        db.collection("matches").whereEqualTo("code", newCode)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    saveMatchCode(currentUser.value!!.id, newCode)
                } else {
                    generateUniqueCode(length)
                }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error al generar el código: $exception"
                //println("Error checking code uniqueness: $exception")
            }
    }

    fun aceptarMatch(code: String) {
        _isLoading.value = true
        val matched_with = currentUser.value!!.id
        val db = FirebaseFirestore.getInstance()

        db.collection("matches").document(code)
            .update(mapOf("status" to "accepted", "matched_with" to matched_with))
            .addOnSuccessListener {
                _isLoading.value = false
                _emparejado.value = true
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error al hacer match: $exception"
            }
    }

    fun verificarSiYaEstaEmparejado() {
        _isLoading.value = true
        val db = FirebaseFirestore.getInstance()

        db.collection("matches")
            .whereEqualTo("user_id", currentUser.value!!.id)
            .whereEqualTo("status", "accepted")
            .get()
            .addOnSuccessListener { snapshot ->
                println("prueba ${snapshot.documents}")
                if (!snapshot.isEmpty) {
                    _emparejado.value = true
                    obtenerDatosPareja(snapshot.documents[0].getString("matched_with")!!)
                } else {
                    verificarSiTieneCodigoGenerado()
                }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error al verificar emparejamiento: $exception"
                println("Error al verificar emparejamiento:$exception")
            }
    }

    fun verificarSiTieneCodigoGenerado() {
        val db = FirebaseFirestore.getInstance()

        db.collection("matches")
            .whereEqualTo("user_id", currentUser.value!!.id)
            .whereEqualTo("status", "pending")
            .whereEqualTo("matched_with", null)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                _isLoading.value = false
                if (!snapshot.isEmpty) {
                    _codigoGenerado.value = snapshot.documents[0].id
                    mostrarOpcionGenerarCodigo()
                }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error al obtener el último código pendiente: $exception"
            }
    }

    fun obtenerDatosPareja(user_id_pareja: String){
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("usuarios").child(user_id_pareja)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _isLoading.value = false
                _pareja.value = snapshot.getValue(User::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {
                _isLoading.value = false
                _errorMessage.value = "Error: ${error.message}"
            }
        })

    }

    fun limpiarMensajeError(){
        _errorMessage.value = null
    }
}