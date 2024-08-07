package com.example.proyectofinal

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Propietario
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast

class Add_Propietario : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseCounterDb: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_propietario)

        // Inicializar DatabaseHelper y Firebase
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("propietarios")
        firebaseCounterDb = FirebaseDatabase.getInstance().getReference("counter")

        // Referencias a los componentes de la UI
        val etNombre = findViewById<EditText>(R.id.et_nombre_propietario)
        val etPuesto = findViewById<EditText>(R.id.et_puesto_propietario)
        val etNumero = findViewById<EditText>(R.id.et_numero_propietario)
        val etStatus = findViewById<EditText>(R.id.et_status_propietario)
        val btnAddPropietario = findViewById<Button>(R.id.btn_add_propietario)
        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configura el onClickListener para el botón
        btnRegresar.setOnClickListener {
            // Finaliza la actividad actual y regresa a la anterior
            finish()
        }

        // Configurar el listener del botón
        btnAddPropietario.setOnClickListener {
            val nombre = etNombre.text.toString()
            val puesto = etPuesto.text.toString().toIntOrNull() ?: 0
            val numero = etNumero.text.toString()
            val status = etStatus.text.toString().toIntOrNull() ?: 0

            if (nombre.isNotEmpty() && numero.isNotEmpty()) {
                // Insertar en Firebase primero para obtener el ID
                addPropietarioToFirebase(Propietario(0, nombre, puesto, numero, status))
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Manejo de Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun addPropietarioToSQLite(propietario: Propietario) {
        try {
            dbHelper.writableDatabase.use { db ->
                val values = ContentValues().apply {
                    put("nombre", propietario.nombre)
                    put("puesto", propietario.puesto)
                    put("numero", propietario.numero)
                    put("status", propietario.status)
                }
                val newRowId = db.insert("Propietario", null, values)

                if (newRowId != -1L) {
                    Toast.makeText(this, "Propietario agregado a SQLite", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al agregar el Propietario a SQLite", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Excepción al agregar el Propietario a SQLite: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun addPropietarioToFirebase(propietario: Propietario) {
        firebaseCounterDb.child("propietarios").get().addOnSuccessListener { snapshot ->
            try {
                val newId = if (snapshot.exists()) snapshot.value.toString().toInt() + 1 else 1

                val propietarioToAdd = propietario.copy(idPropietario = newId)
                val newPropietarioRef = firebaseDb.child(newId.toString())

                newPropietarioRef.setValue(propietarioToAdd).addOnSuccessListener {
                    Toast.makeText(this, "Propietario agregado a Firebase", Toast.LENGTH_SHORT).show()
                    firebaseCounterDb.child("propietarios").setValue(newId)
                    // Insertar en SQLite con el ID correcto
                    addPropietarioToSQLite(propietarioToAdd)
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al agregar el Propietario a Firebase: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Excepción al procesar el contador de Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al obtener el contador de Firebase: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
}
