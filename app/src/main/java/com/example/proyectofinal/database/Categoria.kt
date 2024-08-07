package com.example.proyectofinal.database

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Categoria(
    var idCategoria: Int = 0,
    var nombre: String = "",
    var status: Int = 0
)
