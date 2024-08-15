package com.example.proyectofinal.database


data class Historial(
    val idHistorial: Int= 0,
    val aula: Int = 0,       // Referencia al idLab de Aula
    val categoria: Int = 0,  // Referencia al idCategoria de Categoria
    val componente: Int = 0, // Referencia al idComponente de Componente
    val fecha: String = "",
    val hora: String = "",
    val status: Int = 0
)
