package com.danifitdev.naughtymatch.domain.usecases

import com.danifitdev.naughtymatch.domain.repository.LoginRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class RegisterWithEmailUseCase @Inject constructor(private val repository: LoginRepository) {
    suspend operator fun invoke(email: String, password: String): Result<FirebaseUser?> {
        return repository.registerWithEmail(email, password)
    }
}