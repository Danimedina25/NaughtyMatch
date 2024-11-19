package com.danifitdev.naughtymatch.data.repository

import com.danifitdev.naughtymatch.domain.model.User
import com.google.firebase.auth.FirebaseUser


fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User {
    return User(
        authId = firebaseUser.uid,
        nombre = firebaseUser.displayName,
        correo = firebaseUser.email,
        fotoPerfil = firebaseUser.photoUrl.toString()
    )
}