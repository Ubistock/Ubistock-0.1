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
import com.example.proyectofinal.database.Categoria
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import android.view.ViewGroup
import android.view.Gravity
import android.graphics.Color
import com.example.proyectofinal.R.id.et_nombre_categoria

class Add_Categoria : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseCounterDb: DatabaseReference
    private lateinit var tblCategoriaDetails: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_categoria)

        // Inicializar DatabaseHelper y Firebase
        dbHelper = DatabaseHelper(this)
        firebaseDb = FirebaseDatabase.getInstance().getReference("categorias")
        firebaseCounterDb = FirebaseDatabase.getInstance().getReference("counter")
        //mostrar todos las categorias
        mostrarTodasLasCategorias()

        // Referencias a los componentes de la UI
        val etNombre = findViewById<EditText>(et_nombre_categoria)
        val etStatus = findViewById<EditText>(R.id.et_status_categoria)
        val etCategoriaId = findViewById<EditText>(R.id.et_categoria_id) // Campo de texto para ingresar ID de categoría
        tblCategoriaDetails = findViewById(R.id.tbl_categoria_details) // TableLayout para mostrar detalles de la categoría
        val btnAddCategoria = findViewById<Button>(R.id.btn_add_categoria)
        val btnBuscarCategoria = findViewById<Button>(R.id.btn_buscar_categoria) // Botón para buscar categoría por ID
        val btnMostrarCategorias = findViewById<Button>(R.id.btn_mostrar_categorias) // Botón para mostrar todas las categorías
        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configura el onClickListener para el botón
        btnRegresar.setOnClickListener {
            // Finaliza la actividad actual y regresa a la anterior
            finish()
        }

        // Configurar el listener del botón para agregar categoría
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

        // Configurar el listener del botón para buscar categoría por ID
        btnBuscarCategoria.setOnClickListener {
            val categoriaId = etCategoriaId.text.toString().toIntOrNull()
            if (categoriaId != null) {
                buscarCategoriaPorId(categoriaId) { categoria ->
                    if (categoria != null) {
                        mostrarDetallesCategoriaID(categoria)
                    } else {
                        Toast.makeText(this, "Categoría no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un ID válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón para mostrar todas las categorías
        btnMostrarCategorias.setOnClickListener {
            mostrarTodasLasCategorias()
        }
    }

    private fun buscarCategoriaPorId(id: Int, callback: (Categoria?) -> Unit){
        dbHelper.getCategoriaByIdFirebase(id) { categorias ->
            if (categorias.isNotEmpty()) {
                callback(categorias[0])
            } else {
                callback(null)
            }
        }
    }

    private fun mostrarTodasLasCategorias(){
        dbHelper.getAllCategoriasFromFirebase { categorias ->
            if (categorias.isNotEmpty()) {
                mostrarDetallesCategoria(categorias)
            } else {
                Toast.makeText(this, "No hay categorías registradas", Toast.LENGTH_SHORT).show()
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
                addCategoriaToSQLite(updatedCategoria)
            }.addOnFailureListener {
                Toast.makeText(this, "Error al agregar la Categoría a Firebase: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al obtener el contador de Firebase: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDetallesCategoria(categorias: List<Categoria>) {
        tblCategoriaDetails.removeAllViews()

        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.setBackgroundColor(Color.GRAY)

        val headers = arrayOf("ID", "Nombre", "Status")
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

        tblCategoriaDetails.addView(headerRow)

        categorias.forEach { categoria ->
            val row = TableRow(this)
            row.layoutParams = TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            row.setBackgroundColor(Color.WHITE)

            val details = arrayOf(categoria.idCategoria.toString(), categoria.nombre, categoria.status.toString())
            details.forEach { detail ->
                val textView = TextView(this).apply {
                    text = detail
                    setPadding(8, 8, 8, 8)
                    gravity = Gravity.CENTER
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }
                row.addView(textView)
            }
            tblCategoriaDetails.addView(row)
        }
    }

    private fun mostrarDetallesCategoriaID(categoria: Categoria) {
        tblCategoriaDetails.removeAllViews()

        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerRow.setBackgroundColor(Color.GRAY)

        val headers = arrayOf("ID", "Nombre", "Status")
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

        tblCategoriaDetails.addView(headerRow)

        val row = TableRow(this)
        row.layoutParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        row.setBackgroundColor(Color.WHITE)

        val details = arrayOf(categoria.idCategoria.toString(), categoria.nombre, categoria.status.toString())
        details.forEach { detail ->
            val textView = TextView(this).apply {
                text = detail
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(textView)
        }
        tblCategoriaDetails.addView(row)
    }
}