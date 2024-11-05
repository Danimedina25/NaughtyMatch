package com.danifitdev.naughtymatch.domain.model

data class User(
    val id: Int,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val foto_perfil: String,
    val fecha_nac: String
)
