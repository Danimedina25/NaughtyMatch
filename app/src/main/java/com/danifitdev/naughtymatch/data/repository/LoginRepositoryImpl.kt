package com.danifitdev.naughtymatch.data.repository

import android.util.Log
import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.domain.repository.LoginRepository
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import okhttp3.internal.userAgent
import javax.inject.Inject


class LoginRepositoryImpl  @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : LoginRepository {
    override suspend fun loginWithEmail(email: String, password: String, androidId: String): Result<User?> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            firebaseUser.let {
                val userDataInFirebase = getUserDataInFirebase(firebaseUser!!.uid)
                Result.success(userDataInFirebase)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithFacebook(token: String): Result<User?> {
        val credential = FacebookAuthProvider.getCredential(token)
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val userMapper = mapFirebaseUserToUser(authResult.user!!)
            val userDataInDb = getUserDataInFirebase(userMapper.androidId)
            if(userDataInDb != null){
                userMapper.nombre = userDataInDb.nombre
                userMapper.genero = userDataInDb.genero
                userMapper.fechaNac = userDataInDb.fechaNac
                userMapper.telefono = userDataInDb.telefono
            }
            Result.success(userMapper)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDataInFirebase(authId: String): User? {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("usuarios").child(authId)

        return try {
            val snapshot = userRef.get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            println("Error obteniendo los datos del usuario: ${e.message}")
            null
        }
    }
}
