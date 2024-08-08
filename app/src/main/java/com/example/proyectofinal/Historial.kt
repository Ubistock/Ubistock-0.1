package com.example.proyectofinal

import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Historial

class Historial : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)

        dbHelper = DatabaseHelper(this)
        loadHistorialData()
    }

    private fun loadHistorialData() {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayoutHistorial)
        val historialList: List<Historial> = dbHelper.getAllHistorial()

        for (historial in historialList) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            val idHistorialTextView = TextView(this)
            idHistorialTextView.text = historial.idHistorial.toString()
            idHistorialTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f)
            tableRow.addView(idHistorialTextView)

            val aulaTextView = TextView(this)
            aulaTextView.text = historial.aula.toString()
            aulaTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            tableRow.addView(aulaTextView)

            val categoriaTextView = TextView(this)
            categoriaTextView.text = historial.categoria.toString()
            categoriaTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f)
            tableRow.addView(categoriaTextView)

            val componenteTextView = TextView(this)
            componenteTextView.text = historial.componente.toString()
            componenteTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f)
            tableRow.addView(componenteTextView)

            val fechaTextView = TextView(this)
            fechaTextView.text = historial.fecha
            fechaTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            tableRow.addView(fechaTextView)

            val horaTextView = TextView(this)
            horaTextView.text = historial.hora
            horaTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            tableRow.addView(horaTextView)

            val statusTextView = TextView(this)
            statusTextView.text = historial.status.toString()
            statusTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            tableRow.addView(statusTextView)

            tableLayout.addView(tableRow)
        }
    }
}
