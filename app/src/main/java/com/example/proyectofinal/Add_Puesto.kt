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
import com.example.proyectofinal.database.Puesto
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast

class Add_Puesto : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseCounterDb: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_puesto)

        // Inicializar DatabaseHelper y Firebase
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("puestos")
        firebaseCounterDb = FirebaseDatabase.getInstance().getReference("counter")

        // Referencias a los componentes de la UI
        val etPuesto = findViewById<EditText>(R.id.et_nombre_puesto)
        val etStatus = findViewById<EditText>(R.id.et_status_puesto)
        val btnAddPuesto = findViewById<Button>(R.id.btn_add_puesto)
        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configura el onClickListener para el botón
        btnRegresar.setOnClickListener {
            // Finaliza la actividad actual y regresa a la anterior
            finish()
        }

        // Configurar el listener del botón
        btnAddPuesto.setOnClickListener {
            val puesto = etPuesto.text.toString()
            val status = etStatus.text.toString().toIntOrNull() ?: 0

            if (puesto.isNotEmpty()) {
                // Insertar en Firebase primero para obtener el ID
                addPuestoToFirebase(Puesto(0, puesto, status))
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

    private fun addPuestoToSQLite(puesto: Puesto) {
        try {
            dbHelper.writableDatabase.use { db ->
                val values = ContentValues().apply {
                    put("puesto", puesto.puesto)
                    put("status", puesto.status)
                }
                val newRowId = db.insert("Puesto", null, values)

                if (newRowId != -1L) {
                    Toast.makeText(this, "Puesto agregado a SQLite", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al agregar el Puesto a SQLite", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Excepción al agregar el Puesto a SQLite: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun addPuestoToFirebase(puesto: Puesto) {
        firebaseCounterDb.child("puestos").get().addOnSuccessListener { snapshot ->
            try {
                val newId = if (snapshot.exists()) snapshot.value.toString().toInt() + 1 else 1

                val puestoToAdd = puesto.copy(idRango = newId)
                val newPuestoRef = firebaseDb.child(newId.toString())

                newPuestoRef.setValue(puestoToAdd).addOnSuccessListener {
                    Toast.makeText(this, "Puesto agregado a Firebase", Toast.LENGTH_SHORT).show()
                    firebaseCounterDb.child("puestos").setValue(newId)
                    // Insertar en SQLite con el ID correcto
                    addPuestoToSQLite(puestoToAdd)
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al agregar el Puesto a Firebase: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Excepción al procesar el contador de Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al obtener el contador de Firebase: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }
}