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
        enableEdgeToEdge()
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

    private fun addCategoriaToSQLite(categoria: Categoria) {
        dbHelper.writableDatabase.use { db ->
            val values = ContentValues().apply {
                put("nombre", categoria.nombre)
                put("status", categoria.status)
            }
            val newRowId = db.insert("Categoria", null, values)

            if (newRowId != -1L) {
                Toast.makeText(this, "Categoría agregada a SQLite", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al agregar la Categoría a SQLite", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addCategoriaToFirebase(categoria: Categoria) {
        firebaseCounterDb.child("categorias").get().addOnSuccessListener { snapshot ->
            val newId = if (snapshot.exists()) snapshot.value.toString().toInt() + 1 else 1

            val categoriaToAdd = categoria.copy(idCategoria = newId)
            val newCategoriaRef = firebaseDb.child(newId.toString())

            newCategoriaRef.setValue(categoriaToAdd).addOnSuccessListener {
                Toast.makeText(this, "Categoría agregada a Firebase", Toast.LENGTH_SHORT).show()
                firebaseCounterDb.child("categorias").setValue(newId)
                // Insertar en SQLite con el ID correcto
                addCategoriaToSQLite(categoriaToAdd)
            }.addOnFailureListener {
                Toast.makeText(this, "Error al agregar la Categoría a Firebase", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al obtener el contador de Firebase", Toast.LENGTH_SHORT).show()
        }
    }
}
