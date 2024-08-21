package com.example.proyectofinal.database

data class Componente(
    val idComponente: String="",
    val categoria: Int=0,  // Referencia al idCategoria de Categoria
    val propi: Int=0,      // Referencia al idPropietario de Propietario
    val aula: Int=0,       // Referencia al idLab de Aula
    val status: Int=0
)
