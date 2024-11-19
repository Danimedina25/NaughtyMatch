package com.danifitdev.naughtymatch.domain.repository

import com.danifitdev.naughtymatch.domain.model.User
import com.google.firebase.auth.FirebaseUser

interface LoginRepository {
    suspend fun loginWithEmail(email: String, password: String, androidId:String): Result<User?>
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser?>
    suspend fun loginWithFacebook(token: String): Result<User?>
}