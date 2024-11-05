package com.danifitdev.naughtymatch.data.repository

import com.danifitdev.naughtymatch.domain.model.User
import com.google.firebase.auth.FirebaseUser


fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User {
    return User(
        id = firebaseUser.uid,
        nombre = firebaseUser.displayName,
        correo = firebaseUser.email,
        foto_perfil = firebaseUser.photoUrl.toString()
    )
}