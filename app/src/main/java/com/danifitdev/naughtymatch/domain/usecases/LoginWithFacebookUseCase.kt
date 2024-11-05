package com.danifitdev.naughtymatch.domain.usecases

import com.danifitdev.naughtymatch.domain.model.User
import com.danifitdev.naughtymatch.domain.repository.LoginRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class LoginWithFacebookUseCase  @Inject constructor(private val repository: LoginRepository) {
    suspend operator fun invoke(token: String): Result<User?> {
        return repository.loginWithFacebook(token)
    }
}