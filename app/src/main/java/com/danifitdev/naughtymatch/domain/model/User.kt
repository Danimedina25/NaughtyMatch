package com.danifitdev.naughtymatch.domain.model

data class User(
    var androidId: String = "",
    var authId: String = "",
    var nombre: String? = "",
    var correo: String? = "",
    var telefono: String = "",
    var fotoPerfil: String? = "",
    var fechaNac: String = "",
    var genero: String = "",
)
