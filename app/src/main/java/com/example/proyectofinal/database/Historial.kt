package com.example.proyectofinal.database

data class Historial(
    val idHistorial: Int,
    val aula: Int,       // Referencia al idLab de Aula
    val categoria: Int,  // Referencia al idCategoria de Categoria
    val componente: Int,  // Referencia al idComponente de Componente
    val fecha: String,
    val hora: String,
    val status: Int
)
