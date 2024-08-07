package com.example.proyectofinal.database

data class Historial(
    val idHistorial: Int,
    val aula: Int,       // Referencia al idLab de Aula
    val categoria: Int,  // Referencia al idCategoria de Categoria

    val fecha: String,
    val hora: String,
    val status: Int
)
