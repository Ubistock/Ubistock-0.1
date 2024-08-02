package com.example.proyectofinal.database

data class Componente(
    val idComponente: Int,
    val categoria: Int,  // Referencia al idCategoria de Categoria
    val propi: Int,      // Referencia al idPropietario de Propietario
    val aula: Int,       // Referencia al idLab de Aula
    val status: Int
)
