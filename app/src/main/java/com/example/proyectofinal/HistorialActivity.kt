package com.example.proyectofinal

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Historial
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast

class HistorialActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var aulasDb: DatabaseReference
    private lateinit var categoriasDb: DatabaseReference
    private lateinit var componentesDb: DatabaseReference
    private lateinit var tableLayoutHistorial: TableLayout
    private lateinit var etHistorialId: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        // Inicializar dbHelper antes de llamar a cualquier método que lo use
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("Historial")
        aulasDb = FirebaseDatabase.getInstance().getReference("aulas") // Referencia correcta a 'aulas'
        categoriasDb = FirebaseDatabase.getInstance().getReference("categorias") // Referencia correcta a 'aulas'
        componentesDb = FirebaseDatabase.getInstance().getReference("componentes") // Referencia correcta a 'aulas'
        tableLayoutHistorial = findViewById(R.id.tbl_historial_details)
        etHistorialId = findViewById(R.id.et_historial_id) // EditText para buscar por ID

        val btnBuscarHistorial = findViewById<Button>(R.id.btn_buscar_historial)
        val btnMostrarHistorial = findViewById<Button>(R.id.btn_mostrar_historial)
        val btnRegresar = findViewById<Button>(R.id.btn_regresar)
        mostrarTodosLosHistoriales()

        // Configurar el listener del botón para buscar historial por ID
        btnBuscarHistorial.setOnClickListener {
            val historialId = etHistorialId.text.toString().toIntOrNull()
            if (historialId != null) {
                buscarHistorialPorId(historialId) { historial ->
                    if (historial != null) {
                        mostrarDetallesHistorial(listOf(historial))
                    } else {
                        Toast.makeText(this, "Historial no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un ID válido", Toast.LENGTH_SHORT).show()
            }
        }
        // Configura el onClickListener para el botón
        btnRegresar.setOnClickListener {
            finish()
        }
        // Configurar el listener del botón para mostrar todos los historiales
        btnMostrarHistorial.setOnClickListener {
            mostrarTodosLosHistoriales()
        }
    }

    private fun buscarHistorialPorId(id: Int, callback: (Historial?) -> Unit) {
        dbHelper.getHistorialByIdFirebase(id) { historiales ->
            if (historiales.isNotEmpty()) {
                callback(historiales.first())
            } else {
                callback(null)
            }
        }
    }

    private fun mostrarTodosLosHistoriales() {
        dbHelper.getAllHistorialFromFirebase { historiales ->
            if (historiales.isNotEmpty()) {
                mostrarDetallesHistorial(historiales)
            } else {
                Toast.makeText(this, "No hay historiales registrados", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun obtenerNombreAula(aulaId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firebaseDb.child("aulas").child(aulaId).get().addOnSuccessListener { aulaSnapshot ->
            val nombreAula = aulaSnapshot.child("nombre").value.toString()
            onSuccess(nombreAula)
        }.addOnFailureListener {
            onFailure(it)
        }
    }

    private fun obtenerNombreCategoria(categoriaId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firebaseDb.child("categorias").child(categoriaId).get().addOnSuccessListener { categoriaSnapshot ->
            val nombreCategoria = categoriaSnapshot.child("nombre").value.toString()
            onSuccess(nombreCategoria)
        }.addOnFailureListener {
            onFailure(it)
        }
    }


    private fun mostrarDetallesHistorial(historiales: List<Historial>) {
        tableLayoutHistorial.removeAllViews()

        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(getColor(R.color.gray))

        val headers = arrayOf("Aula", "Categoría","Componente", "Fecha", "Hora")
        headers.forEach { header ->
            val textView = TextView(this).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setTextColor(getColor(R.color.white))
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                textSize = 16f
            }
            headerRow.addView(textView)
        }
        tableLayoutHistorial.addView(headerRow)

        historiales.forEach { historial ->
            val aulaId = historial.aula.toString()
            val categoriaId = historial.categoria.toString()

            // Obtener el nombre del aula
            obtenerNombreAula(aulaId, { nombreAula ->
                // Obtener el nombre de la categoría
                obtenerNombreCategoria(categoriaId, { nombreCategoria ->
                    // Mostrar los detalles en la tabla
                    val row = TableRow(this)
                    row.setBackgroundColor(getColor(R.color.white))

                    val details = arrayOf(
                        historial.aula.toString(),
                        historial.categoria.toString(),
                        historial.componente.toString(),
                        historial.fecha,
                        historial.hora
                    )
                    details.forEach { detail ->
                        val textView = TextView(this).apply {
                            text = detail
                            setPadding(8, 8, 8, 8)
                            gravity = Gravity.CENTER
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                            textSize = 16f
                        }
                        row.addView(textView)
                    }
                    tableLayoutHistorial.addView(row)
                }, {
                    Toast.makeText(this, "Error al obtener el nombre de la categoría", Toast.LENGTH_SHORT).show()
                })
            }, {
                Toast.makeText(this, "Error al obtener el nombre del aula", Toast.LENGTH_SHORT).show()
            })
        }
    }
}
