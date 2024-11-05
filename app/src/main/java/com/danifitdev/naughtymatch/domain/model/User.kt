package com.danifitdev.naughtymatch.domain.model

data class User(
    var id: String = "",
    var nombre: String? = "",
    var correo: String? = "",
    var telefono: String = "",
    var foto_perfil: String? = "",
    var fecha_nac: String = "",
    var genero: String = ""
)
