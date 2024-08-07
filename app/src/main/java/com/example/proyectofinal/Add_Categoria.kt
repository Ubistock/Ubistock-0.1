package com.example.proyectofinal

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Categoria
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast

class Add_Categoria : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseCounterDb: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_categoria)

        // Inicializar DatabaseHelper y Firebase
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("categorias")
        firebaseCounterDb = FirebaseDatabase.getInstance().getReference("counter")

        // Referencias a los componentes de la UI
        val etNombre = findViewById<EditText>(R.id.et_nombre_categoria)
        val etStatus = findViewById<EditText>(R.id.et_status_categoria)
        val btnAddCategoria = findViewById<Button>(R.id.btn_add_categoria)

        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configura el onClickListener para el botón
        btnRegresar.setOnClickListener {
            // Finaliza la actividad actual y regresa a la anterior
            finish()
        }

        // Configurar el listener del botón
        btnAddCategoria.setOnClickListener {
            val nombre = etNombre.text.toString()
            val status = etStatus.text.toString().toIntOrNull() ?: 0

            if (nombre.isNotEmpty()) {
                // Insertar en Firebase primero para obtener el ID
                addCategoriaToFirebase(Categoria(0, nombre, status))
                Toast.makeText(this, "Categoría agregada con éxito.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCategoriaToSQLite(categoria: Categoria) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombre", categoria.nombre)
            put("status", categoria.status)
        }
        val newRowId = db.insert("Categoria", null, values)
        db.close()

        if (newRowId != -1L) {
            Toast.makeText(this, "Categoría agregada a SQLite", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error al agregar la Categoría a SQLite", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCategoriaToFirebase(categoria: Categoria) {
        firebaseCounterDb.child("categorias").get().addOnSuccessListener { snapshot ->
            var newId = 1
            if (snapshot.exists()) {
                newId = snapshot.value.toString().toInt() + 1
            }

            val updatedCategoria = categoria.copy(idCategoria = newId)
            val categoriaId = firebaseDb.child(newId.toString()).key ?: return@addOnSuccessListener
            firebaseDb.child(categoriaId).setValue(updatedCategoria).addOnSuccessListener {
                Toast.makeText(this, "Categoría agregada a Firebase", Toast.LENGTH_SHORT).show()
                firebaseCounterDb.child("categorias").setValue(newId)
                // Insertar en SQLite con el ID correcto
                addCategoriaToSQLite(updatedCategoria)
            }.addOnFailureListener {
                Toast.makeText(this, "Error al agregar la Categoría a Firebase", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener el contador de Firebase", Toast.LENGTH_SHORT).show()
        }
    }
}
