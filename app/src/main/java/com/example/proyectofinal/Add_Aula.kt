package com.example.proyectofinal

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Aula
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast

class Add_Aula : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseCounterDb: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_aula)

        // Inicializar DatabaseHelper y Firebase
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("aulas")
        firebaseCounterDb = FirebaseDatabase.getInstance().getReference("counter")

        // Referencias a los componentes de la UI
        val etEdificio = findViewById<EditText>(R.id.et_edificio_aula)
        val etNombre = findViewById<EditText>(R.id.et_nombre_aula)
        val etStatus = findViewById<EditText>(R.id.et_status_aula)
        val btnAddAula = findViewById<Button>(R.id.btn_add_aula)

        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configura el onClickListener para el botón
        btnRegresar.setOnClickListener {
            // Finaliza la actividad actual y regresa a la anterior
            finish()
        }
        // Configurar el listener del botón
        btnAddAula.setOnClickListener {
            val edificio = etEdificio.text.toString()
            val nombre = etNombre.text.toString()
            val status = etStatus.text.toString().toIntOrNull() ?: 0

            if (edificio.isNotEmpty() && nombre.isNotEmpty()) {
                // Insertar en Firebase primero para obtener el ID
                addAulaToFirebase(Aula(0, edificio, nombre, status))
                Toast.makeText(this, "Aula agregada con exito.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Error al agregar el Aula a Firebase", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener el contador de Firebase", Toast.LENGTH_SHORT).show()
        }
    }
}
