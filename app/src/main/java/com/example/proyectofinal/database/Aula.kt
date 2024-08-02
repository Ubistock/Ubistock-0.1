package com.example.proyectofinal.database

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Aula(
    var idLab: Int = 0,
    var edificio: String = "",
    var nombre: String = "",
    var status: Int = 0
)
