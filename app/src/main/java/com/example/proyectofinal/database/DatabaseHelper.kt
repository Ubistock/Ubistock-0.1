package com.example.proyectofinal.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "miInventario.db"
        private const val DATABASE_VERSION = 2  // Incrementa la versión
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
                componente INTEGER,
                fecha TEXT,
                hora TEXT,
                status INTEGER,
                FOREIGN KEY (aula) REFERENCES Aula(idLab),
                FOREIGN KEY (categoria) REFERENCES Categoria(idCategoria),
                FOREIGN KEY (componente) REFERENCES Componente(idComponente)
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

    // Añadir historial
    fun insertHistorial(aula: Int, categoria: Int, componente: Int, status: Int) {
        val db = this.writableDatabase
        val values = ContentValues()

        val currentDateTime = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        values.put("aula", aula)
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
                    val aulaId = cursor.getInt(cursor.getColumnIndexOrThrow("aula"))
                    val categoriaId = cursor.getInt(cursor.getColumnIndexOrThrow("categoria"))
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
            val cursor = db.query("Aula", arrayOf("nombre"), "idLab=?", arrayOf(aulaId.toString()), null, null, null)
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
            val cursor = db.query("Categoria", arrayOf("nombre"), "idCategoria=?", arrayOf(categoriaId.toString()), null, null, null)
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
            val cursor = db.query("Aula", null, "idLab=?", arrayOf(id.toString()), null, null, null)
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

    fun getAllAulas(): List<Aula> {
        val aulasList = mutableListOf<Aula>()
        val db = this.readableDatabase
        try {
            val cursor = db.query("Aula", null, null, null, null, null, null)
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