package com.example.proyectofinal

import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.DatabaseHelper

class Historial : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tableLayoutHistorial: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)

        dbHelper = DatabaseHelper(this)
        tableLayoutHistorial = findViewById(R.id.tableLayoutHistorial)

        loadHistorialData()
    }

    private fun loadHistorialData() {
        val historialList = dbHelper.getAllHistorial()
        for (historial in historialList) {
            val tableRow = TableRow(this)

            // Crear y agregar TextViews para cada columna
            val idTextView = TextView(this)
            idTextView.text = historial["idHistorial"]
            tableRow.addView(idTextView)

            val aulaTextView = TextView(this)
            aulaTextView.text = historial["aula"]
            tableRow.addView(aulaTextView)

            val categoriaTextView = TextView(this)
            categoriaTextView.text = historial["categoria"]
            tableRow.addView(categoriaTextView)

            val fechaTextView = TextView(this)
            fechaTextView.text = historial["fecha"]
            tableRow.addView(fechaTextView)

            val horaTextView = TextView(this)
            horaTextView.text = historial["hora"]
            tableRow.addView(horaTextView)

            val statusTextView = TextView(this)
            statusTextView.text = historial["status"]
            tableRow.addView(statusTextView)

            // Agregar la fila a la TableLayout
            tableLayoutHistorial.addView(tableRow)
        }
    }
}