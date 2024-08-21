package com.example.proyectofinal.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "miInventario.db"
        private const val DATABASE_VERSION = 3  // Incrementa la versión
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tablas
        db.execSQL("""
            CREATE TABLE puestos (
                idRango INTEGER PRIMARY KEY AUTOINCREMENT,
                puesto TEXT,
                status INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE propietarios (
                idPropietario INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                puesto INTEGER,
                numero TEXT,
                status INTEGER,
                FOREIGN KEY (puesto) REFERENCES puestos(idRango)
            )
        """)
        db.execSQL("""
            CREATE TABLE categorias (
                idCategoria INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                status INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE aulas (
                idLab INTEGER PRIMARY KEY AUTOINCREMENT,
                edificio TEXT,
                nombre TEXT,
                status INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE componentes (
                idComponente INTEGER PRIMARY KEY AUTOINCREMENT,
                categoria INTEGER,
                propi INTEGER,
                aula INTEGER,
                status INTEGER,
                FOREIGN KEY (categoria) REFERENCES categorias(idCategoria),
                FOREIGN KEY (propi) REFERENCES propietarios(idPropietario),
                FOREIGN KEY (aula) REFERENCES aulas(idLab)
            )
        """)
        db.execSQL("""
            CREATE TABLE Historial (
                idHistorial INTEGER PRIMARY KEY AUTOINCREMENT,
                aula INTEGER,
                categoria INTEGER,
                componente INTEGER,
                fecha TEXT,
                hora TEXT,
                status INTEGER,
                FOREIGN KEY (aula) REFERENCES aulas(idLab),
                FOREIGN KEY (categoria) REFERENCES categorias(idCategoria),
                FOREIGN KEY (componente) REFERENCES componentes(idComponente)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Eliminar tablas existentes
        db.execSQL("DROP TABLE IF EXISTS categorias")
        db.execSQL("DROP TABLE IF EXISTS aulas")
        db.execSQL("DROP TABLE IF EXISTS componentes")
        db.execSQL("DROP TABLE IF EXISTS Historial")
        db.execSQL("DROP TABLE IF EXISTS propietarios")
        db.execSQL("DROP TABLE IF EXISTS puestos")
        onCreate(db)
    }

    // Añadir historial
    fun insertHistorial(aula: Int, categoria: Int, componente: Int, status: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        val currentDateTime = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        values.put("aulas", aula)
        values.put("categoria", categoria)
        values.put("componente", componente)
        values.put("fecha", dateFormat.format(currentDateTime))
        values.put("hora", timeFormat.format(currentDateTime))
        values.put("status", status)

        db.insert("Historial", null, values)
        db.close()
    }

    fun getAllHistorial(): List<Map<String, String>> {
        val historialList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase

        try {
            val cursor = db.query("Historial", null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val idHistorial = cursor.getInt(cursor.getColumnIndexOrThrow("idHistorial"))
                    val aulaId = cursor.getInt(cursor.getColumnIndexOrThrow("aulas"))
                    val categoriaId = cursor.getInt(cursor.getColumnIndexOrThrow("categoria"))
                    val componenteId = cursor.getInt(cursor.getColumnIndexOrThrow("idComponentes"))
                    val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                    val hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"))
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow("status"))

                    // Obtener los nombres a partir de los IDs
                    val aulaName = getAulaNameById(aulaId)
                    val categoriaName = getCategoriaNameById(categoriaId)

                    // Crear un mapa para almacenar los datos
                    val historialData = mapOf(
                        "idHistorial" to idHistorial.toString(),
                        "aula" to aulaName,
                        "categoria" to categoriaName,
                        "fecha" to fecha,
                        "hora" to hora,
                        "status" to status.toString()
                    )

                    historialList.add(historialData)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return historialList
    }

    fun getAulaNameById(aulaId: Int): String {
        val db = this.readableDatabase
        var name = ""
        try {
            val cursor = db.query("aulas", arrayOf("nombre"), "idLab=?", arrayOf(aulaId.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return name
    }

    fun getCategoriaNameById(categoriaId: Int): String {
        val db = this.readableDatabase
        var name = ""
        try {
            val cursor = db.query("categorias", arrayOf("nombre"), "idCategoria=?", arrayOf(categoriaId.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return name
    }
    fun getAulaById(id: Int): Aula? {
        val db = this.readableDatabase
        var aula: Aula? = null
        try {
            val cursor = db.query("aulas", null, "idLab=?", arrayOf(id.toString()), null, null, null)
            if (cursor.moveToFirst()) {
                val idLab = cursor.getInt(cursor.getColumnIndexOrThrow("idLab"))
                val edificio = cursor.getString(cursor.getColumnIndexOrThrow("edificio"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                val status = cursor.getInt(cursor.getColumnIndexOrThrow("status"))
                aula = Aula(idLab, edificio, nombre, status)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return aula
    }
    /*fun getAulaByIdFirebase(id: Int, callback: (List<Aula>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("aulas")

        databaseReference.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val aula = dataSnapshot.getValue(Aula::class.java)
                // Crear una lista con el aula encontrada o vacía si no se encuentra
                val aulasList = if (aula != null) listOf(aula) else emptyList()
                callback(aulasList)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Llamar al callback con una lista vacía en caso de error
                callback(emptyList())
            }
        })
    }*/
    // En DatabaseHelper.kt o en una clase similar
    fun getAulaByIdFirebase(id: Int, callback: (List<Aula>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("aulas")

        databaseReference.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val aula = dataSnapshot.getValue(Aula::class.java)
                // Filtrar por idLab igual a 0
                val aulasList = if (aula != null && aula.status == 0) listOf(aula) else emptyList()
                callback(aulasList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Llamar al callback con una lista vacía en caso de error
                callback(emptyList())
            }
        })
    }
    /*fun getAllAulasFromFirebase(callback: (List<Aula>) -> Unit) {
        val aulasList = mutableListOf<Aula>()
        val databaseReference = FirebaseDatabase.getInstance().getReference("aulas")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                aulasList.clear()
                // Limpiar lista antes de añadir nuevos datos
                for (snapshot in dataSnapshot.children) {
                    val aula = snapshot.getValue(Aula::class.java)
                    aula?.let {
                        aulasList.add(it)
                    }
                }
                // Llamar al callback con la lista de aulas
                callback(aulasList)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
            }
        })
    }*/
    fun getAllAulasFromFirebase(callback: (List<Aula>) -> Unit) {
        val aulasList = mutableListOf<Aula>()
        val databaseReference = FirebaseDatabase.getInstance().getReference("aulas")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                aulasList.clear()
                // Limpiar lista antes de añadir nuevos datos
                for (snapshot in dataSnapshot.children) {
                    val aula = snapshot.getValue(Aula::class.java)
                    aula?.let {
                        // Añadir solo las aulas con idLab igual a 0
                        if (it.status == 0) {
                            aulasList.add(it)
                        }
                    }
                }
                // Llamar al callback con la lista filtrada de aulas
                callback(aulasList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Llamar al callback con una lista vacía en caso de error
                callback(emptyList())
            }
        })
    }
    fun getCategoriaByIdFirebase(id: Int, callback: (List<Categoria>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("categorias")

        databaseReference.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categoria = dataSnapshot.getValue(Categoria::class.java)
                // Crear una lista con la categoría encontrada o vacía si no se encuentra
                val categoriasList = if (categoria != null) listOf(categoria) else emptyList()
                callback(categoriasList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Llamar al callback con una lista vacía en caso de error
                callback(emptyList())
            }
        })
    }
    fun getAllCategoriasFromFirebase(callback: (List<Categoria>) -> Unit) {
        val categoriasList = mutableListOf<Categoria>()
        val databaseReference = FirebaseDatabase.getInstance().getReference("categorias")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                categoriasList.clear()
                // Limpiar lista antes de añadir nuevos datos
                for (snapshot in dataSnapshot.children) {
                    val categoria = snapshot.getValue(Categoria::class.java)
                    categoria?.let {
                        categoriasList.add(it)
                    }
                }
                // Llamar al callback con la lista de categorías
                callback(categoriasList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
            }
        })
    }
    fun getComponenteByIdFirebase(id: String, callback: (List<Componente>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("componentes")

        databaseReference.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val componente = dataSnapshot.getValue(Componente::class.java)
                // Crear una lista con el componente encontrado o vacía si no se encuentra
                val componentesList = if (componente != null) listOf(componente) else emptyList()
                callback(componentesList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Llamar al callback con una lista vacía en caso de error
                callback(emptyList())
            }
        })
    }
    fun getAllComponentesFromFirebase(callback: (List<Componente>) -> Unit) {
        val componentesList = mutableListOf<Componente>()
        val databaseReference = FirebaseDatabase.getInstance().getReference("componentes")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                componentesList.clear()
                // Limpiar lista antes de añadir nuevos datos
                for (snapshot in dataSnapshot.children) {
                    val componente = snapshot.getValue(Componente::class.java)
                    componente?.let {
                        componentesList.add(it)
                    }
                }
                // Llamar al callback con la lista de componentes
                callback(componentesList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Puedes decidir si quieres pasar una lista vacía o manejar el error de otra forma
                callback(emptyList())
            }
        })
    }

    fun getAllPropietariosFromFirebase(callback: (List<Propietario>) -> Unit) {
        val propietariosList = mutableListOf<Propietario>()
        val databaseReference = FirebaseDatabase.getInstance().getReference("propietarios")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                propietariosList.clear()
                // Limpiar lista antes de añadir nuevos datos
                for (snapshot in dataSnapshot.children) {
                    val categoria = snapshot.getValue(Propietario::class.java)
                    categoria?.let {
                        propietariosList.add(it)
                    }
                }
                // Llamar al callback con la lista de propietarios
                callback(propietariosList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
            }
        })
    }
    fun getPropietarioByIdFirebase(id: Int, callback: (List<Propietario>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("propietarios")

        databaseReference.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val propietario = dataSnapshot.getValue(Propietario::class.java)
                // Crear una lista con el propietario encontrada o vacía si no se encuentra
                val propietarioList = if (propietario != null) listOf(propietario) else emptyList()
                callback(propietarioList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Llamar al callback con una lista vacía en caso de error
                callback(emptyList())
            }
        })
    }
    // Función para obtener todos los puestos desde Firebase
    fun getAllPuestosFromFirebase(callback: (List<Puesto>) -> Unit) {
        val puestosList = mutableListOf<Puesto>()
        val databaseReference = FirebaseDatabase.getInstance().getReference("puestos")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                puestosList.clear()  // Limpiar lista antes de añadir nuevos datos
                for (snapshot in dataSnapshot.children) {
                    val puesto = snapshot.getValue(Puesto::class.java)
                    puesto?.let {
                        puestosList.add(it)
                    }
                }
                // Llamar al callback con la lista de puestos
                callback(puestosList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
            }
        })
    }

    // Función para obtener un puesto por ID desde Firebase
    fun getPuestoByIdFirebase(id: Int, callback: (List<Puesto>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("puestos")

        databaseReference.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val puesto = dataSnapshot.getValue(Puesto::class.java)
                // Crear una lista con el puesto encontrado o vacía si no se encuentra
                val puestoList = if (puesto != null) listOf(puesto) else emptyList()
                callback(puestoList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Llamar al callback con una lista vacía en caso de error
                callback(emptyList())
            }
        })
    }
    fun getAllHistorialFromFirebase(callback: (List<Historial>) -> Unit) {
        val historialList = mutableListOf<Historial>()
        val databaseReference = FirebaseDatabase.getInstance().getReference("Historial")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                historialList.clear()  // Limpiar lista antes de añadir nuevos datos
                for (snapshot in dataSnapshot.children) {
                    val historial = snapshot.getValue(Historial::class.java)
                    historial?.let {
                        historialList.add(it)
                    }
                }
                // Llamar al callback con la lista de historiales
                callback(historialList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
            }
        })
    }
    fun updateHistorialInFirebase(id: Int, updatedHistorial: Historial, callback: (Boolean) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Historial")

        databaseReference.child(id.toString()).setValue(updatedHistorial)
            .addOnSuccessListener {
                // Llamar al callback con true si la actualización fue exitosa
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("DatabaseError", "Error al actualizar el historial: ${exception.message}")
                // Llamar al callback con false en caso de error
                callback(false)
            }
    }
    // Función para obtener un Historial por ID desde Firebase
    fun getHistorialByIdFirebase(id: Int, callback: (List<Historial>) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Historial")

        databaseReference.child(id.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val Historial = dataSnapshot.getValue(Historial::class.java)
                // Crear una lista con el puesto encontrado o vacía si no se encuentra
                val HistorialList = if (Historial != null) listOf(Historial) else emptyList()
                callback(HistorialList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                // Llamar al callback con una lista vacía en caso de error
                callback(emptyList())
            }
        })
    }

    fun getAllAulas(): List<Aula> {
        val aulasList = mutableListOf<Aula>()
        val db = this.readableDatabase
        try {
            val cursor = db.query("aulas", null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val idLab = cursor.getInt(cursor.getColumnIndexOrThrow("idLab"))
                    val edificio = cursor.getString(cursor.getColumnIndexOrThrow("edificio"))
                    val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow("status"))
                    aulasList.add(Aula(idLab, edificio, nombre, status))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return aulasList
    }
}