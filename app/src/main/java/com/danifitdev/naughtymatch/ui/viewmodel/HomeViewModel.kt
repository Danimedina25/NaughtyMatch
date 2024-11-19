package com.danifitdev.naughtymatch.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.domain.usecases.RegisterWithEmailUseCase
import com.danifitdev.naughtymatch.utils.UserPreferencesRepository
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
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
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    @ApplicationContext private val appContext: Context
): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _codigoGenerado = MutableStateFlow("")
    val codigoGenerado: StateFlow<String> = _codigoGenerado

    val isLoggedIn: StateFlow<Boolean> = userPreferencesRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentUser: StateFlow<User?> = userPreferencesRepository.user
        .stateIn(viewModelScope, SharingStarted.Eagerly, User())

    private val _opcionIngresarCodigo = MutableStateFlow(false)
    val opcionIngresarCodigo: StateFlow<Boolean> = _opcionIngresarCodigo

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _messageLoader = MutableStateFlow<String?>("Cargando...")
    val messageLoader: StateFlow<String?> get() = _messageLoader

    private val _pareja = MutableStateFlow(User())
    val pareja: StateFlow<User> = _pareja

    private val _editarPerfil = MutableStateFlow(false)
    val editarPerfil: StateFlow<Boolean> = _editarPerfil

    private val _puedenJugar = MutableStateFlow(true)
    val puedenJugar: StateFlow<Boolean> = _puedenJugar

    private val _registroExitoso= MutableStateFlow(false)
    val registroExitoso: StateFlow<Boolean> = _registroExitoso

    private val _firebaseDatabaseInstance = FirebaseDatabase.getInstance()
    private val _firebaseFirestoreInstance = FirebaseFirestore.getInstance()
    private val _firebaseAuthInstance =  FirebaseAuth.getInstance()
    private val _firebaseStorageInstance = FirebaseStorage.getInstance()

    private val _androidId = MutableStateFlow("")
    val androidId: StateFlow<String> = _androidId

    init {
        getAndroidId()
    }

    fun getAndroidId(){
        _androidId.value = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun setLoading(loading: Boolean){
        _isLoading.value = loading
    }

    fun mostrarOpcionIngresarCodigo(){
        _opcionIngresarCodigo.value = !_opcionIngresarCodigo.value
    }

    fun setEditarPerfil(editarPerfil: Boolean){
        _editarPerfil.value = editarPerfil
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

    fun logout(onNavigateToLogin:()->Unit) {
        viewModelScope.launch {
            try {
                _firebaseAuthInstance.signOut()
                LoginManager.getInstance().logOut()
                userPreferencesRepository.setLoggedIn(false)
                updateUser(User())
                onNavigateToLogin()
            }catch (e:Exception){
                _errorMessage.value = "Error al intentar cerrar sesión, ${e.message}"
            }
        }
    }

    suspend fun actualizarInformacion(imageUri: Uri?, user: User): Boolean {
        val firebaseUser = _firebaseAuthInstance.currentUser
        val storageRef = _firebaseStorageInstance.reference.child("profilePictures/${firebaseUser!!.uid}.jpg")
        _firebaseAuthInstance.languageCode
        return try {
            val profileUpdates: UserProfileChangeRequest
            if(imageUri != null){
                val uploadTask = storageRef.putFile(imageUri)
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

            firebaseUser.updateProfile(profileUpdates).await()
            user.authId = firebaseUser.uid
            user.fotoPerfil = firebaseUser.photoUrl.toString()
            updateUserInFirebase(user)
            true
        } catch (e: Exception) {
            _errorMessage.value = "Error actualizando el perfil: ${e.message}"
            _isLoading.value = false
            false
        }
    }

    suspend fun updateUserInFirebase(user: User) {
        val database = _firebaseDatabaseInstance.getReference("usuarios").child(user.authId)
        val userMap = mapOf(
            "androidId" to user.androidId,
            "authId" to user.authId,
            "nombre" to user.nombre,
            "correo" to user.correo,
            "telefono" to user.telefono,
            "fotoPerfil" to user.fotoPerfil,
            "fechaNac" to user.fechaNac,
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
        val db = _firebaseFirestoreInstance
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
        val db = _firebaseFirestoreInstance
        val newCode = generateRandomCode(length)

        db.collection("matches").document(newCode)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    saveMatchCode(androidId.value, newCode)
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

    suspend fun obtenerUserIdDeMiPareja(codigoMatch: String): String {
        _isLoading.value = true
        return try {
            val document = _firebaseFirestoreInstance
                .collection("matches")
                .document(codigoMatch)
                .get()
                .await()

            if (document.exists()) {
                val user_id = document.getString("user_id") ?: return ""
                user_id
            } else {
                _isLoading.value = false
                _errorMessage.value = "No se encontró el código ingresado"
                ""
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.message
            e.printStackTrace()
            ""
        }
    }

    fun hacerMatch(codigo: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val matched_with: String
            val user_id = obtenerUserIdDeMiPareja(codigo)
            if (codigo.equals(_codigoGenerado.value)) {
                _isLoading.value = false
                _errorMessage.value = "No puedes ingresar el mismo código que generaste"
                return@launch
            } else
            if (isLoggedIn.value) {
                matched_with = currentUser.value!!.authId
                if (!verificarSiMiParejaEstaRegistrada(user_id)){
                    _isLoading.value = false
                    _errorMessage.value = "Ups... pídele a tu pareja que también se registre para poder hacer match"
                    return@launch
                }
            } else {
                matched_with = androidId.value
                if (verificarSiMiParejaEstaRegistrada(user_id)){
                    _isLoading.value = false
                    _errorMessage.value = "Ups..regístrate como lo hizo tu pareja para para poder hacer match"
                    return@launch
                }
            }
            val db = _firebaseFirestoreInstance
            db.collection("matches").document(codigo)
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
    }

    suspend fun verificarSiMiParejaEstaRegistrada(user_id: String): Boolean{
        val snapshot = _firebaseDatabaseInstance.reference
            .child("usuarios")
            .child(user_id)
            .get()
            .await()
        return snapshot.exists()
    }

    suspend fun verificarSiYaEstaEmparejado() {
        _isLoading.value = true
        val db = _firebaseFirestoreInstance
        val collection = db.collection("matches")
        var userId = ""
        if(isLoggedIn.value){
            userId =  currentUser.value!!.authId
        }else{
            userId = _androidId.value
        }
        try {
            val queryByUserId = collection
                .whereEqualTo("user_id", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            if (!queryByUserId.isEmpty) {
                _isLoading.value = false
                _emparejado.value = true
                val user_id = queryByUserId.documents[0].getString("matched_with")!!
                if(isLoggedIn.value && !verificarSiMiParejaEstaRegistrada(user_id)){
                    _puedenJugar.value = false
                    monitorearSiMiParejaSeRegistra(user_id)
                }
                //obtenerDatosPareja(snapshot.documents[0].getString("matched_with")!!)
                return
            }

            val queryByMatchedWith = collection
                .whereEqualTo("matched_with", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .await()

            if (!queryByMatchedWith.isEmpty) {
                _isLoading.value = false
                _emparejado.value = true
                val user_id = queryByUserId.documents[0].getString("user_id")!!
                if(isLoggedIn.value && !verificarSiMiParejaEstaRegistrada(user_id)){
                    _puedenJugar.value = false
                    monitorearSiMiParejaSeRegistra(user_id)
                }
                //obtenerDatosPareja(snapshot.documents[0].getString("matched_with")!!)
                return
            }
            verificarSiTieneCodigoGenerado()
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Error al verificar emparejamiento: ${e.localizedMessage}"
            println("Error al verificar emparejamiento: ${e.localizedMessage}")
        }
    }

    fun verificarSiTieneCodigoGenerado() {
        val db = _firebaseFirestoreInstance

        db.collection("matches")
            .whereEqualTo("user_id", androidId.value)
            .whereEqualTo("status", "pending")
            .whereEqualTo("matched_with", null)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                _isLoading.value = false
                if (!snapshot.isEmpty) {
                    _codigoGenerado.value = snapshot.documents[0].id
                }else{
                    setMessageLoader("Generando código...")
                    generateUniqueCode(6)
                }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Error al obtener el último código pendiente: $exception"
            }
    }

    fun obtenerDatosPareja(user_id_pareja: String){
        val database = _firebaseDatabaseInstance
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

    fun registerWithEmail(email: String, password: String, imageUri: Uri?, user: User) = viewModelScope.launch {
        _isLoading.value = true
        viewModelScope.launch {
            val result = registerWithEmailUseCase(email, password)
            if (result.isSuccess) {
                val updateData = actualizarInformacion(imageUri, user)
                if(updateData){
                    updateAuthIdInMatchesCodes()
                }else{
                    _isLoading.value = false
                    _errorMessage.value = "Ocurrió un error al intentar registrar tu información"
                }
            } else {
                _isLoading.value = false
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    suspend fun updateAuthIdInMatchesCodes(){
        if(_emparejado.value){
            val db = _firebaseFirestoreInstance
            val collection = db.collection("matches")
            try {
                val query1 = collection
                    .whereEqualTo("user_id", _androidId.value)
                    .whereEqualTo("status", "accepted")
                    .get()
                    .await()
                if (!query1.isEmpty) {
                    val update = mapOf("user_id" to _firebaseAuthInstance.currentUser!!.uid)
                    update.let { query1.documents[0].reference.update(it).await() }
                    _isLoading.value = false
                    _registroExitoso.value = true
                    return
                }

                val query2 = collection
                    .whereEqualTo("matched_with", _androidId.value)
                    .whereEqualTo("status", "accepted")
                    .get()
                    .await()
                if (!query2.isEmpty) {
                    val update = mapOf("matched_with" to currentUser.value!!.authId)
                    update.let { query1.documents[0].reference.update(it).await() }
                    _isLoading.value = false
                    _registroExitoso.value = true
                    return
                }

            }catch (e: Exception){
                _isLoading.value = false
                _errorMessage.value = "Error al actualizar el authId en el match: ${e.message}"
            }
        }else{
            val firebaseUser = _firebaseAuthInstance.currentUser
            val db = _firebaseFirestoreInstance

            db.collection("matches").document(_codigoGenerado.value)
                .update(mapOf("user_id" to firebaseUser!!.uid))
                .addOnSuccessListener {
                    _isLoading.value = false
                    _registroExitoso.value = true
                }
                .addOnFailureListener { exception ->
                    _isLoading.value = false
                    _errorMessage.value = "Error al actualizar el authId en el código generado: ${exception.message}"
                }
        }
    }

    fun validarDatosRegistro(imageUri: Uri?, nombre: String, email: String, genero: String,
                             fechaNacimiento: String, password: String, confirmPassword: String){
        if(nombre.isEmpty()){
            _errorMessage.value = "Por favor, ingresa tu nombre completo"
            return
        }
        if(email.isEmpty()){
            _errorMessage.value = "Por favor, ingresa tu email"
            return
        }
        if(genero.isEmpty()){
            _errorMessage.value = "Por favor, selecciona tu género"
            return
        }
        if(fechaNacimiento.isEmpty()){
            _errorMessage.value = "Por favor, ingresa tu fecha de nacimiento"
            return
        }
        if (!emailPattern.matcher(email).matches()) {
            _errorMessage.value = "Por favor, ingresa un correo electrónico válido"
            return
        }
        if(password.isEmpty()){
            _errorMessage.value = "Por favor, ingresa una contraseña"
            return
        }
        if(confirmPassword.isEmpty()){
            _errorMessage.value = "Por favor, confirma tu contraseña"
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        if(confirmPassword != password){
            _errorMessage.value = "Las contraseñas no coinciden"
            return
        }
        val user = User(androidId.value, "", nombre, email, "", "", fechaNacimiento, genero)
        registerWithEmail(email, password, imageUri, user)
    }

    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )

    fun monitorearSiMiParejaSeRegistra(authId: String) {
        val database = _firebaseDatabaseInstance.reference
        database.child("usuarios").child(authId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    _puedenJugar.value = true
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Error al monitorear: ${error.message}")
            }
        })
    }
}