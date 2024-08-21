package com.example.proyectofinal

import android.content.ContentValues
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Puesto
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.view.ViewGroup
import android.view.Gravity
import android.graphics.Color
import com.example.proyectofinal.Add_Componente.SpinnerItem

class Add_Puesto : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseCounterDb: DatabaseReference
    private lateinit var tblPuestoDetails: TableLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_puesto)

        // Initialize DatabaseHelper and Firebase
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("puestos")
        firebaseCounterDb = FirebaseDatabase.getInstance().getReference("counter")

        //mostrar todos los puestos
        mostrarTodosLosPuestos()
        // UI Components References
        val etPuesto = findViewById<EditText>(R.id.et_nombre_puesto)
        val etStatus = findViewById<EditText>(R.id.et_status_puesto)
        val etPuestoId = findViewById<EditText>(R.id.et_puesto_id) // TextField for entering Puesto ID
        tblPuestoDetails = findViewById(R.id.tbl_puesto_details) // TableLayout for displaying Puesto details
        val btnAddPuesto = findViewById<Button>(R.id.btn_add_puesto)
        val btnBuscarPuesto = findViewById<Button>(R.id.btn_buscar_puesto) // Button to search Puesto by ID
        val btnMostrarPuestos = findViewById<Button>(R.id.btn_mostrar_puestos) // Button to show all Puestos
        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configure back button to finish activity
        btnRegresar.setOnClickListener {
            finish()
        }

        // Configure the button listener to add Puesto
        btnAddPuesto.setOnClickListener {
            val puesto = etPuesto.text.toString()
            val status = etStatus.text.toString().toIntOrNull() ?: 0

            if (puesto.isNotEmpty()) {
                addPuestoToFirebase(Puesto(0, puesto, status))
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configure the button listener to search Puesto by ID
        btnBuscarPuesto.setOnClickListener {
            val puestoId = etPuestoId.text.toString().toIntOrNull()
            if (puestoId != null) {
                buscarPuestoPorId(puestoId) { puesto ->
                    if (puesto != null) {
                        mostrarDetallesPuestoID(puesto)
                    } else {
                        Toast.makeText(this, "Puesto no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un ID válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Configure the button listener to show all Puestos
        btnMostrarPuestos.setOnClickListener {
            mostrarTodosLosPuestos()
        }
    }

    private fun buscarPuestoPorId(id: Int, callback: (Puesto?) -> Unit) {
        dbHelper.getPuestoByIdFirebase(id) { puestos ->
            if (puestos.isNotEmpty()) {
                callback(puestos[0])
            } else {
                callback(null)
            }
        }
    }

    private fun mostrarTodosLosPuestos() {
        dbHelper.getAllPuestosFromFirebase { puestos ->
            if (puestos.isNotEmpty()) {
                mostrarDetallesPuesto(puestos)
            } else {
                Toast.makeText(this, "No hay puestos registrados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addPuestoToSQLite(puesto: Puesto) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("puesto", puesto.puesto)
            put("status", puesto.status)
        }
        val newRowId = db.insert("Puesto", null, values)
        db.close()

        if (newRowId != -1L) {
            Toast.makeText(this, "Puesto agregado a SQLite", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al agregar el Puesto a SQLite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addPuestoToFirebase(puesto: Puesto) {
        firebaseCounterDb.child("puestos").get().addOnSuccessListener { snapshot ->
            var newId = 1
            if (snapshot.exists()) {
                newId = snapshot.value.toString().toInt() + 1
            }

            val updatedPuesto = puesto.copy(idRango = newId)
            val puestoId = firebaseDb.child(newId.toString()).key ?: return@addOnSuccessListener
            firebaseDb.child(puestoId).setValue(updatedPuesto).addOnSuccessListener {
                Toast.makeText(this, "Puesto agregado a Firebase", Toast.LENGTH_SHORT).show()
                firebaseCounterDb.child("puestos").setValue(newId)
                addPuestoToSQLite(updatedPuesto)
            }.addOnFailureListener {
                Toast.makeText(this, "Error al agregar el Puesto a Firebase: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al obtener el contador de Firebase: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDetallesPuesto(puestos: List<Puesto>) {
        tblPuestoDetails.removeAllViews()

        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.setBackgroundColor(Color.GRAY)

        val headers = arrayOf("ID", "Puesto", "Status")
        headers.forEach { header ->
            val textView = TextView(this).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            headerRow.addView(textView)
        }

        tblPuestoDetails.addView(headerRow)

        puestos.forEach { puesto ->
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            row.setBackgroundColor(Color.WHITE)

            val details = arrayOf(puesto.idRango.toString(), puesto.puesto, puesto.status.toString())
            details.forEach { detail ->
                val textView = TextView(this).apply {
                    text = detail
                    setPadding(8, 8, 8, 8)
                    gravity = Gravity.CENTER
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }
                row.addView(textView)
            }
            tblPuestoDetails.addView(row)
        }
    }

    private fun mostrarDetallesPuestoID(puesto: Puesto) {
        tblPuestoDetails.removeAllViews()

        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.setBackgroundColor(Color.GRAY)

        val headers = arrayOf("ID", "Puesto", "Status")
        headers.forEach { header ->
            val textView = TextView(this).apply {
                text = header
                setPadding(8, 8, 8, 8)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            headerRow.addView(textView)
        }

        tblPuestoDetails.addView(headerRow)

        val row = TableRow(this)
        row.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        row.setBackgroundColor(Color.WHITE)

        val details = arrayOf(puesto.idRango.toString(), puesto.puesto, puesto.status.toString())
        details.forEach { detail ->
            val textView = TextView(this).apply {
                text = detail
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(textView)
        }
        tblPuestoDetails.addView(row)
    }
}