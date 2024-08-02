package com.example.proyectofinal.database

data class Propietario(
    val idPropietario: Int,
    val nombre: String,
    val puesto: Int,  // Referencia al idRango de Puesto
    val numero: String,
    val status: Int
)
