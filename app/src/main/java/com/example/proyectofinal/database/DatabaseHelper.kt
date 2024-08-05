package com.example.proyectofinal.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "miInventario.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tablas
        db.execSQL("""
        CREATE TABLE Puesto (
            idPuesto INTEGER PRIMARY KEY AUTOINCREMENT,
            puesto TEXT,
            status INTEGER
        )
    """)
        db.execSQL("""
        CREATE TABLE Propietario (
            idPropietario INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT,
            puesto INTEGER,
            numero TEXT,
            status INTEGER,
            FOREIGN KEY (puesto) REFERENCES Puesto(idPuesto)
        )
    """)
        db.execSQL("""
        CREATE TABLE Categoria (
            idCategoria INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT,
            status INTEGER
        )
    """)
        db.execSQL("""
        CREATE TABLE Aula (
            idLab INTEGER PRIMARY KEY AUTOINCREMENT,
            edificio TEXT,
            nombre TEXT,
            status INTEGER
        )
    """)
        db.execSQL("""
        CREATE TABLE Componente (
            idComponente INTEGER PRIMARY KEY AUTOINCREMENT,
            categoria INTEGER,
            propi INTEGER,
            aula INTEGER,
            status INTEGER,
            FOREIGN KEY (categoria) REFERENCES Categoria(idCategoria),
            FOREIGN KEY (propi) REFERENCES Propietario(idPropietario),
            FOREIGN KEY (aula) REFERENCES Aula(idLab)
        )
    """)
        db.execSQL("""
        CREATE TABLE Historial (
            idHistorial INTEGER PRIMARY KEY AUTOINCREMENT,
            aula INTEGER,
            categoria INTEGER,
            fecha TEXT,
            hora TEXT,
            status INTEGER,
            FOREIGN KEY (aula) REFERENCES Aula(idLab),
            FOREIGN KEY (categoria) REFERENCES Categoria(idCategoria)
        )
    """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Eliminar tablas existentes
        db.execSQL("DROP TABLE IF EXISTS Categoria")
        db.execSQL("DROP TABLE IF EXISTS Aula")
        db.execSQL("DROP TABLE IF EXISTS Componente")
        db.execSQL("DROP TABLE IF EXISTS Historial")
        db.execSQL("DROP TABLE IF EXISTS Propietario")
        db.execSQL("DROP TABLE IF EXISTS Puesto")
        onCreate(db)
    }

    // Insertar una nueva categoría
    fun addCategoria(categoria: Categoria): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", categoria.nombre)
            put("status", categoria.status)
        }
        return db.insert("Categoria", null, values)
    }

    // Obtener todas las categorías
    fun getCategorias(): List<Categoria> {
        val db = readableDatabase
        val cursor = db.query("Categoria", null, null, null, null, null, null)
        val categorias = mutableListOf<Categoria>()
        with(cursor) {
            while (moveToNext()) {
                val idCategoria = getInt(getColumnIndexOrThrow("idCategoria"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val status = getInt(getColumnIndexOrThrow("status"))
                categorias.add(Categoria(idCategoria, nombre, status))
            }
        }
        cursor.close()
        return categorias
    }

    // Métodos para Aula
    fun addAula(aula: Aula): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("edificio", aula.edificio)
            put("nombre", aula.nombre)
            put("status", aula.status)
        }
        return db.insert("Aula", null, values)
    }

    fun getAulas(): List<Aula> {
        val db = readableDatabase
        val cursor = db.query("Aula", null, null, null, null, null, null)
        val aulas = mutableListOf<Aula>()
        with(cursor) {
            while (moveToNext()) {
                val idLab = getInt(getColumnIndexOrThrow("idLab"))
                val edificio = getString(getColumnIndexOrThrow("edificio"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val status = getInt(getColumnIndexOrThrow("status"))
                aulas.add(Aula(idLab, edificio, nombre, status))
            }
        }
        cursor.close()
        return aulas
    }
    // Insertar un nuevo componente
    fun addComponente(componente: Componente): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("categoria", componente.categoria)
            put("propi", componente.propi)
            put("aula", componente.aula)
            put("status", componente.status)
        }
        return db.insert("Componente", null, values)
    }

    // Obtener todos los componentes
    fun getComponentes(): List<Componente> {
        val db = readableDatabase
        val cursor = db.query("Componente", null, null, null, null, null, null)
        val componentes = mutableListOf<Componente>()
        with(cursor) {
            while (moveToNext()) {
                val idComponente = getInt(getColumnIndexOrThrow("idComponente"))
                val categoria = getInt(getColumnIndexOrThrow("categoria"))
                val propi = getInt(getColumnIndexOrThrow("propi"))
                val aula = getInt(getColumnIndexOrThrow("aula"))
                val status = getInt(getColumnIndexOrThrow("status"))
                componentes.add(Componente(idComponente, categoria, propi, aula, status))
            }
        }
        cursor.close()
        return componentes
    }

    // Insertar un nuevo historial
    fun addHistorial(historial: Historial): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("aula", historial.aula)
            put("categoria", historial.categoria)
            put("fecha", historial.fecha)
            put("hora", historial.hora)
            put("status", historial.status)
        }
        return db.insert("Historial", null, values)
    }

    // Obtener todos los historiales
    fun getHistoriales(): List<Historial> {
        val db = readableDatabase
        val cursor = db.query("Historial", null, null, null, null, null, null)
        val historiales = mutableListOf<Historial>()
        with(cursor) {
            while (moveToNext()) {
                val idHistorial = getInt(getColumnIndexOrThrow("idHistorial"))
                val aula = getInt(getColumnIndexOrThrow("aula"))
                val categoria = getInt(getColumnIndexOrThrow("categoria"))
                val fecha = getString(getColumnIndexOrThrow("fecha"))
                val hora = getString(getColumnIndexOrThrow("hora"))
                val status = getInt(getColumnIndexOrThrow("status"))
                historiales.add(Historial(idHistorial, aula, categoria, fecha, hora, status))
            }
        }
        cursor.close()
        return historiales
    }

    // Insertar un nuevo propietario
    fun addPropietario(propietario: Propietario): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", propietario.nombre)
            put("puesto", propietario.puesto)
            put("numero", propietario.numero)
            put("status", propietario.status)
        }
        return db.insert("Propietario", null, values)
    }

    // Obtener todos los propietarios
    fun getPropietarios(): List<Propietario> {
        val db = readableDatabase
        val cursor = db.query("Propietario", null, null, null, null, null, null)
        val propietarios = mutableListOf<Propietario>()
        with(cursor) {
            while (moveToNext()) {
                val idPropietario = getInt(getColumnIndexOrThrow("idPropietario"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val puesto = getInt(getColumnIndexOrThrow("puesto"))
                val numero = getString(getColumnIndexOrThrow("numero"))
                val status = getInt(getColumnIndexOrThrow("status"))
                propietarios.add(Propietario(idPropietario, nombre, puesto, numero, status))
            }
        }
        cursor.close()
        return propietarios
    }

    // Insertar un nuevo puesto
    fun addPuesto(puesto: Puesto): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("puesto", puesto.puesto)
            put("status", puesto.status)
        }
        return db.insert("Puesto", null, values)
    }

    // Obtener todos los puestos
    fun getPuestos(): List<Puesto> {
        val db = readableDatabase
        val cursor = db.query("Puesto", null, null, null, null, null, null)
        val puestos = mutableListOf<Puesto>()
        with(cursor) {
            while (moveToNext()) {
                val idPuesto = getInt(getColumnIndexOrThrow("idPuesto"))
                val puesto = getString(getColumnIndexOrThrow("puesto"))
                val status = getInt(getColumnIndexOrThrow("status"))
                puestos.add(Puesto(idPuesto, puesto, status))
            }
        }
        cursor.close()
        return puestos
    }
}
