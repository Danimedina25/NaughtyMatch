package com.danifitdev.naughtymatch.data.repository

import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.domain.repository.LoginRepository
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class LoginRepositoryImpl  @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : LoginRepository {
    override suspend fun loginWithEmail(email: String, password: String): Result<User?> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            firebaseUser.let {
                Result.success(mapFirebaseUserToUser(it!!))
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
            Result.success(userMapper)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
