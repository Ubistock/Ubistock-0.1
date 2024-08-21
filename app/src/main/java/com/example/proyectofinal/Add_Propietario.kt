package com.example.proyectofinal

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Propietario
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import android.view.ViewGroup
import android.view.Gravity
import android.graphics.Color

class Add_Propietario : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseCounterDb: DatabaseReference
    private lateinit var tblPropietarioDetails: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_propietario)

        // Inicializar DatabaseHelper y Firebase
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("propietarios")
        firebaseCounterDb = FirebaseDatabase.getInstance().getReference("counter")
        //mostrar todos los puestos
        mostrarTodosLosPropietarios()
        // Referencias a los componentes de la UI
        val etNombre = findViewById<EditText>(R.id.et_nombre_propietario)
        val etNumero = findViewById<EditText>(R.id.et_numero_propietario)
        val etPuesto = findViewById<EditText>(R.id.et_puesto)
        val etStatus = findViewById<EditText>(R.id.et_status_propietario)
        val etPropietarioId = findViewById<EditText>(R.id.et_propietario_id) // Campo de texto para ingresar ID de propietario
        tblPropietarioDetails = findViewById(R.id.tbl_propietario_details) // TableLayout para mostrar detalles del propietario
        val btnAddPropietario = findViewById<Button>(R.id.btn_add_propietario)
        val btnBuscarPropietario = findViewById<Button>(R.id.btn_buscar_propietario) // Botón para buscar propietario por ID
        val btnMostrarPropietarios = findViewById<Button>(R.id.btn_mostrar_propietarios) // Botón para mostrar todos los propietarios
        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configura el onClickListener para el botón
        btnRegresar.setOnClickListener {
            finish() // Finaliza la actividad actual y regresa a la anterior
        }

        // Configurar el listener del botón para agregar propietario
        btnAddPropietario.setOnClickListener {
            val nombre = etNombre.text.toString()
            val puesto = etPuesto.text.toString().toIntOrNull() ?: 0
            val numero = etNumero.text.toString()
            val status = etStatus.text.toString().toIntOrNull() ?: 0

            if (nombre.isNotEmpty() && numero.isNotEmpty()) {
                // Insertar en Firebase primero para obtener el ID
                addPropietarioToFirebase(Propietario(0, nombre, puesto, numero, status))
                Toast.makeText(this, "Propietario agregado con éxito.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón para buscar propietario por ID
        btnBuscarPropietario.setOnClickListener {
            val propietarioId = etPropietarioId.text.toString().toIntOrNull()
            if (propietarioId != null) {
                buscarPropietarioPorId(propietarioId) { propietario ->
                    if (propietario != null) {
                        mostrarDetallesPropietarioID(propietario)
                    } else {
                        Toast.makeText(this, "Propietario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un ID válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón para mostrar todos los propietarios
        btnMostrarPropietarios.setOnClickListener {
            mostrarTodosLosPropietarios()
        }
    }

    private fun buscarPropietarioPorId(id: Int, callback: (Propietario?) -> Unit) {
        dbHelper.getPropietarioByIdFirebase(id) { propietarios ->
            if (propietarios.isNotEmpty()) {
                callback(propietarios[0])
            } else {
                callback(null)
            }
        }
    }

    private fun mostrarTodosLosPropietarios() {
        dbHelper.getAllPropietariosFromFirebase { propietarios ->
            if (propietarios.isNotEmpty()) {
                mostrarDetallesPropietario(propietarios)
            } else {
                Toast.makeText(this, "No hay propietarios registrados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addPropietarioToSQLite(propietario: Propietario) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombre", propietario.nombre)
            put("puesto", propietario.puesto)
            put("numero", propietario.numero)
            put("status", propietario.status)
        }
        val newRowId = db.insert("propietarios", null, values)
        db.close()

        if (newRowId != -1L) {
            Toast.makeText(this, "Propietario agregado a SQLite", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al agregar el Propietario a SQLite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addPropietarioToFirebase(propietario: Propietario) {
        firebaseCounterDb.child("propietarios").get().addOnSuccessListener { snapshot ->
            var newId = 1
            if (snapshot.exists()) {
                newId = snapshot.value.toString().toInt() + 1
            }

            val updatedPropietario = propietario.copy(idPropietario = newId)
            val propietarioId = firebaseDb.child(newId.toString()).key ?: return@addOnSuccessListener
            firebaseDb.child(propietarioId).setValue(updatedPropietario).addOnSuccessListener {
                Toast.makeText(this, "Propietario agregado a Firebase", Toast.LENGTH_SHORT).show()
                firebaseCounterDb.child("propietarios").setValue(newId)
                addPropietarioToSQLite(updatedPropietario)
            }.addOnFailureListener {
                Toast.makeText(this, "Error al agregar el Propietario a Firebase: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al obtener el contador de Firebase: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDetallesPropietario(propietarios: List<Propietario>) {
        tblPropietarioDetails.removeAllViews()

        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.setBackgroundColor(Color.GRAY)

        val headers = arrayOf("ID", "Nombre", "Puesto", "Número", "Status")
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

        tblPropietarioDetails.addView(headerRow)

        propietarios.forEach { propietario ->
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            row.setBackgroundColor(Color.WHITE)

            val details = arrayOf(
                propietario.idPropietario.toString(),
                propietario.nombre,
                propietario.puesto.toString(),
                propietario.numero,
                propietario.status.toString()
            )
            details.forEach { detail ->
                val textView = TextView(this).apply {
                    text = detail
                    setPadding(8, 8, 8, 8)
                    gravity = Gravity.CENTER
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }
                row.addView(textView)
            }
            tblPropietarioDetails.addView(row)
        }
    }

    private fun mostrarDetallesPropietarioID(propietario: Propietario) {
        tblPropietarioDetails.removeAllViews()

        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.setBackgroundColor(Color.GRAY)

        val headers = arrayOf("ID", "Nombre", "Puesto", "Número", "Status")
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

        tblPropietarioDetails.addView(headerRow)

        val row = TableRow(this)
        row.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        row.setBackgroundColor(Color.WHITE)

        val details = arrayOf(
            propietario.idPropietario.toString(),
            propietario.nombre,
            propietario.puesto.toString(),
            propietario.numero,
            propietario.status.toString()
        )
        details.forEach { detail ->
            val textView = TextView(this).apply {
                text = detail
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(textView)
        }
        tblPropietarioDetails.addView(row)
    }
}
