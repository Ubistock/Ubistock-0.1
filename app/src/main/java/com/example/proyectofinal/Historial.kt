package com.example.proyectofinal

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Historial
import com.example.proyectofinal.database.HistorialAdapter

class Historial : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var historialAdapter: HistorialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Añadir un nuevo historial automáticamente
        addNewHistorialAutomatically()

        loadHistorialData()
    }
    private fun addNewHistorialAutomatically() {
        val aula = 1 // Valor ficticio, debes reemplazarlo por el valor real si es necesario
        val categoria = 1 // Valor ficticio, debes reemplazarlo por el valor real si es necesario
        val componente = 1 // Valor ficticio, debes reemplazarlo por el valor real si es necesario
        val status = 1 // Valor ficticio, debes reemplazarlo por el valor real si es necesario

        dbHelper.insertHistorial(aula, categoria, componente, status)
    }

    private fun loadHistorialData() {
        val historialList: List<Historial> = dbHelper.getAllHistorial()
        historialAdapter = HistorialAdapter(historialList)
        recyclerView.adapter = historialAdapter
    }
}
