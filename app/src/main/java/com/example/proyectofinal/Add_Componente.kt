package com.example.proyectofinal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.Componente
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import android.view.Gravity
import android.graphics.Color
import android.widget.ArrayAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class Add_Componente : AppCompatActivity() {

    private lateinit var firebaseDb: DatabaseReference
    private lateinit var firebaseRFIDDb: DatabaseReference
    private lateinit var tblComponenteDetails: TableLayout

    private lateinit var categoriasList: List<SpinnerItem>
    private lateinit var aulasList: List<SpinnerItem>
    private lateinit var propietariosList: List<SpinnerItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_componente)

        // Inicializar Firebase
        firebaseDb = FirebaseDatabase.getInstance().getReference("componentes")
        firebaseRFIDDb = FirebaseDatabase.getInstance().getReference("RFID")

        // Mostrar todos los componentes al iniciar la actividad
        mostrarTodosLosComponentes()

        // Inicializar listas vacías para los Spinners
        categoriasList = emptyList()
        aulasList = emptyList()
        propietariosList = emptyList()

        // Cargar datos de Firebase para los Spinners
        cargarCategoriasDesdeFirebase()
        cargarAulasDesdeFirebase()
        cargarPropietariosDesdeFirebase()

        // Referencias a los componentes de la UI
        val etIdComponente = findViewById<EditText>(R.id.et_id_componente)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        val spinnerAula = findViewById<Spinner>(R.id.spinner_aula)
        val spinnerPropietario = findViewById<Spinner>(R.id.spinner_propi)
        val etStatusComponente = findViewById<EditText>(R.id.et_status_componente)
        val etComponenteId = findViewById<EditText>(R.id.et_componente_id)
        tblComponenteDetails = findViewById(R.id.tbl_componente_details)
        val btnAddComponente = findViewById<Button>(R.id.btn_add_componente)
        val btnBuscarComponente = findViewById<Button>(R.id.btn_buscar_componente)
        val btnMostrarComponentes = findViewById<Button>(R.id.btn_mostrar_componentes)
        val btnObtenerRFID = findViewById<Button>(R.id.btn_obtener_rfid) // Nuevo botón para obtener RFID
        val btnRegresar: Button = findViewById(R.id.btnRegresar)

        // Configurar el botón de regresar
        btnRegresar.setOnClickListener { finish() }

        // Configurar el botón para obtener RFID
        btnObtenerRFID.setOnClickListener {
            obtenerRFID()
        }

        // Configurar el listener del botón para agregar componente
        btnAddComponente.setOnClickListener {
            val idComponente = etIdComponente.text.toString()
            val categoriaId = (spinnerCategoria.selectedItem as SpinnerItem).id
            val aulaId = (spinnerAula.selectedItem as SpinnerItem).id
            val propietarioId = (spinnerPropietario.selectedItem as SpinnerItem).id
            val status = etStatusComponente.text.toString().toIntOrNull() ?: 0

            if (idComponente.isNotEmpty()) {
                addComponenteToFirebase(Componente(idComponente, categoriaId, propietarioId, aulaId, status))
                Toast.makeText(this, "Componente agregado con éxito.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón para buscar componente por ID
        btnBuscarComponente.setOnClickListener {
            val componenteId = etComponenteId.text.toString().toIntOrNull()
            if (componenteId != null) {
                buscarComponentePorId(componenteId) { componente ->
                    if (componente != null) {
                        mostrarDetallesComponenteID(componente)
                    } else {
                        Toast.makeText(this, "Componente no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, ingrese un ID válido", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón para mostrar todos los componentes
        btnMostrarComponentes.setOnClickListener {
            mostrarTodosLosComponentes()
        }
    }

    private fun obtenerRFID() {
        firebaseRFIDDb.orderByKey().limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rfidId = snapshot.children.lastOrNull()?.getValue(String::class.java)
                if (rfidId != null) {
                    val etIdComponente = findViewById<EditText>(R.id.et_id_componente)
                    etIdComponente.setText(rfidId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_Componente, "Error al obtener RFID: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarAulasDesdeFirebase() {
        val aulasRef = FirebaseDatabase.getInstance().getReference("aulas")
        aulasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val aulas = mutableListOf<SpinnerItem>()
                for (aulaSnapshot in snapshot.children) {
                    val id = aulaSnapshot.child("idLab").getValue(Int::class.java) ?: continue
                    val nombre = aulaSnapshot.child("nombre").getValue(String::class.java) ?: continue
                    aulas.add(SpinnerItem(id, nombre))
                }
                aulasList = aulas
                setupSpinners() // Actualiza los spinners después de cargar los datos
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_Componente, "Error al cargar aulas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun cargarPropietariosDesdeFirebase() {
        val propietariosRef = FirebaseDatabase.getInstance().getReference("propietarios")
        propietariosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val propietarios = mutableListOf<SpinnerItem>()
                for (propietarioSnapshot in snapshot.children) {
                    val id = propietarioSnapshot.child("idPropietario").getValue(Int::class.java) ?: continue
                    val nombre = propietarioSnapshot.child("nombre").getValue(String::class.java) ?: continue
                    propietarios.add(SpinnerItem(id, nombre))
                }
                propietariosList = propietarios
                setupSpinners() // Actualiza los spinners después de cargar los datos
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_Componente, "Error al cargar propietarios: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addComponenteToFirebase(componente: Componente) {
        firebaseDb.child(componente.idComponente).setValue(componente).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Componente agregado exitosamente a Firebase.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al agregar el componente: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarCategoriasDesdeFirebase() {
        val categoriasRef = FirebaseDatabase.getInstance().getReference("categorias")
        categoriasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categorias = mutableListOf<SpinnerItem>()
                for (categoriaSnapshot in snapshot.children) {
                    val id = categoriaSnapshot.child("idCategoria").getValue(Int::class.java) ?: continue
                    val nombre = categoriaSnapshot.child("nombre").getValue(String::class.java) ?: continue
                    categorias.add(SpinnerItem(id, nombre))
                }
                categoriasList = categorias
                setupSpinners()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_Componente, "Error al cargar categorías: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buscarComponentePorId(componenteId: Int, callback: (Componente?) -> Unit) {
        firebaseDb.child(componenteId.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val componente = snapshot.getValue(Componente::class.java)
                callback(componente)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_Componente, "Error al buscar componente: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        })
    }

    private fun mostrarDetallesComponenteID(componente: Componente) {
        val etIdComponente = findViewById<EditText>(R.id.et_id_componente)
        val etStatusComponente = findViewById<EditText>(R.id.et_status_componente)
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        val spinnerAula = findViewById<Spinner>(R.id.spinner_aula)
        val spinnerPropietario = findViewById<Spinner>(R.id.spinner_propi)

        etIdComponente.setText(componente.idComponente)
        etStatusComponente.setText(componente.status.toString())

        // Seleccionar la categoría, aula y propietario en los spinners
        spinnerCategoria.setSelection(categoriasList.indexOfFirst { it.id == componente.categoria })
        spinnerAula.setSelection(aulasList.indexOfFirst { it.id == componente.aula })
        spinnerPropietario.setSelection(propietariosList.indexOfFirst { it.id == componente.propi })
    }

    private fun mostrarTodosLosComponentes() {
        firebaseDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la tabla antes de agregar nuevos datos
                tblComponenteDetails.removeAllViews()

                if (snapshot.exists()) {
                    // Crear una fila de encabezado
                    val headerRow = TableRow(this@Add_Componente)
                    val headers = arrayOf("ID Componente", "Categoría", "Propietario", "Aula", "Status")
                    headers.forEach {
                        val textView = TextView(this@Add_Componente)
                        textView.text = it
                        textView.setPadding(16, 16, 16, 16)
                        textView.setBackgroundColor(Color.LTGRAY)
                        textView.gravity = Gravity.CENTER
                        headerRow.addView(textView)
                    }
                    tblComponenteDetails.addView(headerRow)

                    for (componenteSnapshot in snapshot.children) {
                        val componente = componenteSnapshot.getValue(Componente::class.java)
                        if (componente != null) {
                            val row = TableRow(this@Add_Componente)
                            val values = arrayOf(
                                componente.idComponente,
                                categoriasList.find { it.id == componente.categoria }?.nombre ?: "Desconocida",
                                propietariosList.find { it.id == componente.propi }?.nombre ?: "Desconocido",
                                aulasList.find { it.id == componente.aula }?.nombre ?: "Desconocida",
                                componente.status.toString()
                            )
                            values.forEach {
                                val textView = TextView(this@Add_Componente)
                                textView.text = it
                                textView.setPadding(16, 16, 16, 16)
                                textView.gravity = Gravity.CENTER
                                row.addView(textView)
                            }
                            tblComponenteDetails.addView(row)
                        }
                    }
                } else {
                    // Mostrar un mensaje si no hay datos
                    val noDataRow = TableRow(this@Add_Componente)
                    val noDataTextView = TextView(this@Add_Componente)
                    noDataTextView.text = "No hay componentes disponibles."
                    noDataTextView.gravity = Gravity.CENTER
                    noDataRow.addView(noDataTextView)
                    tblComponenteDetails.addView(noDataRow)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Add_Componente, "Error al cargar los componentes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSpinners() {
        val spinnerCategoria = findViewById<Spinner>(R.id.spinner_categoria)
        val spinnerAula = findViewById<Spinner>(R.id.spinner_aula)
        val spinnerPropietario = findViewById<Spinner>(R.id.spinner_propi)

        // Configurar adaptadores para los spinners
        val categoriasAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriasList)
        categoriasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = categoriasAdapter

        val aulasAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, aulasList)
        aulasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAula.adapter = aulasAdapter

        val propietariosAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, propietariosList)
        propietariosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPropietario.adapter = propietariosAdapter
    }

    // Clase para representar elementos de Spinner
    data class SpinnerItem(val id: Int, val nombre: String) {
        override fun toString(): String {
            return nombre
        }
    }
}