package com.example.proyectofinal.database

data class Propietario(
    val idPropietario: Int=0,
    val nombre: String="",
    val puesto: Int=0,  // Referencia al idRango de Puesto
    val numero: String="",
    val status: Int=0
)
