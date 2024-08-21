package com.example.proyectofinal

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Aula
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import android.view.ViewGroup
import android.view.Gravity
import android.graphics.Color
import com.example.proyectofinal.R.id.et_nombre_aula

class Add_Aula : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseCounterDb: DatabaseReference
    private lateinit var tblAulaDetails: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_aula)

        // Inicializar DatabaseHelper y Firebase
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("aulas")
        firebaseCounterDb = FirebaseDatabase.getInstance().getReference("counter")

        //Mostrar todas las aulas
        mostrarTodasLasAulas()
        // Referencias a los componentes de la UI
        val etEdificio = findViewById<EditText>(R.id.et_edificio_aula)
        val etNombre = findViewById<EditText>(et_nombre_aula)
        val etStatus = findViewById<EditText>(R.id.et_status_aula)
        val etAulaId = findViewById<EditText>(R.id.et_aula_id) // Campo de texto para ingresar ID de aula
        tblAulaDetails = findViewById(R.id.tbl_aula_details) // TableLayout para mostrar detalles del aula
        val btnAddAula = findViewById<Button>(R.id.btn_add_aula)
        val btnBuscarAula = findViewById<Button>(R.id.btn_buscar_aula) // Botón para buscar aula por ID
        val btnMostrarAulas = findViewById<Button>(R.id.btn_mostrar_aulas) // Botón para mostrar todas las aulas
        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configura el onClickListener para el botón
        btnRegresar.setOnClickListener {
            // Finaliza la actividad actual y regresa a la anterior
            finish()
        }

        // Configurar el listener del botón para agregar aula
        btnAddAula.setOnClickListener {
            val edificio = etEdificio.text.toString()
            val nombre = etNombre.text.toString()
            val status = etStatus.text.toString().toIntOrNull() ?: 0

            if (edificio.isNotEmpty() && nombre.isNotEmpty()) {
                // Insertar en Firebase primero para obtener el ID
                addAulaToFirebase(Aula(0, edificio, nombre, status))
                Toast.makeText(this, "Aula agregada con éxito.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón para buscar aula por ID
        btnBuscarAula.setOnClickListener {
            val aulaId = etAulaId.text.toString().toIntOrNull()
            if (aulaId != null) {
                buscarAulaPorId(aulaId) { aula ->
                    if (aula != null) {
                        mostrarDetallesAulaID(aula)
                    } else {
                        Toast.makeText(this, "Aula no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un ID válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón para mostrar todas las aulas
        btnMostrarAulas.setOnClickListener {
            mostrarTodasLasAulas()
        }
    }
    private fun buscarAulaPorId(id: Int, callback: (Aula?) -> Unit){
        dbHelper.getAulaByIdFirebase(id) { aulas ->
            if (aulas.isNotEmpty()) {
                // Solo debe haber un aula con este ID, así que tomamos el primero
                callback(aulas[0])
            } else {
                callback(null)
            }
        }
    }

    private fun mostrarTodasLasAulas(){
        dbHelper.getAllAulasFromFirebase { aulas ->
            if (aulas.isNotEmpty()) {
                mostrarDetallesAula(aulas)
            } else {
                Toast.makeText(this, "No hay aulas registradas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addAulaToSQLite(aula: Aula) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("edificio", aula.edificio)
            put("nombre", aula.nombre)
            put("status", aula.status)
        }
        val newRowId = db.insert("Aula", null, values)
        db.close()

        if (newRowId != -1L) {
            Toast.makeText(this, "Aula agregada a SQLite", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al agregar el Aula a SQLite", Toast.LENGTH_SHORT).show()
        }
    }
    private fun addAulaToFirebase(aula: Aula) {
        firebaseCounterDb.child("aulas").get().addOnSuccessListener { snapshot ->
            var newId = 1
            if (snapshot.exists()) {
                newId = snapshot.value.toString().toInt() + 1
            }

            val updatedAula = aula.copy(idLab = newId)
            val aulaId = firebaseDb.child(newId.toString()).key ?: return@addOnSuccessListener
            firebaseDb.child(aulaId).setValue(updatedAula).addOnSuccessListener {
                Toast.makeText(this, "Aula agregada a Firebase", Toast.LENGTH_SHORT).show()
                firebaseCounterDb.child("aulas").setValue(newId)
                // Insertar en SQLite con el ID correcto
                addAulaToSQLite(updatedAula)
            }.addOnFailureListener {
                Log.e("AddAulaActivity", "Error al agregar el Aula a Firebase", it)
                Toast.makeText(this, "Error al agregar el Aula a Firebase: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("AddAulaActivity", "Error al obtener el contador de Firebase", exception)
            Toast.makeText(this, "Error al obtener el contador de Firebase: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun mostrarDetallesAula(aulas: List<Aula>) {
        // Limpiar la tabla antes de agregar nuevas filas
        tblAulaDetails.removeAllViews()

        // Agregar la fila de encabezados de la tabla
        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.setBackgroundColor(Color.GRAY)

        val headers = arrayOf("ID", "Edificio", "Nombre", "Status")
        headers.forEach { header ->
            val textView = TextView(this).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f) // Distribuye uniformemente el ancho
            }
            headerRow.addView(textView)
        }

        tblAulaDetails.addView(headerRow)

        // Agregar las filas de detalles de las aulas
        aulas.forEach { aula ->
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            row.setBackgroundColor(Color.WHITE)

            val details = arrayOf(aula.idLab.toString(), aula.edificio, aula.nombre, aula.status.toString())
            details.forEach { detail ->
                val textView = TextView(this).apply {
                    text = detail
                    setPadding(8, 8, 8, 8)
                    gravity = Gravity.CENTER
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f) // Distribuye uniformemente el ancho
                }
                row.addView(textView)
            }
            tblAulaDetails.addView(row)
        }
    }
    private fun mostrarDetallesAulaID(aula: Aula) {
        // Limpiar la tabla antes de agregar nuevas filas
        tblAulaDetails.removeAllViews()

        // Agregar la fila de encabezados de la tabla
        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.setBackgroundColor(Color.GRAY)

        val headers = arrayOf("ID", "Edificio", "Nombre", "Status")
        headers.forEach { header ->
            val textView = TextView(this).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                ) // Distribuye uniformemente el ancho
            }
            headerRow.addView(textView)
        }

        tblAulaDetails.addView(headerRow)

        // Agregar la fila de detalles del aula
        val row = TableRow(this)
        row.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        row.setBackgroundColor(Color.WHITE)

        val details =
            arrayOf(aula.idLab.toString(), aula.edificio, aula.nombre, aula.status.toString())
        details.forEach { detail ->
            val textView = TextView(this).apply {
                text = detail
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                ) // Distribuye uniformemente el ancho
            }
            row.addView(textView)
        }
        tblAulaDetails.addView(row)
    }
}
