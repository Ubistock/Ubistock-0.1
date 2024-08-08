package com.example.proyectofinal.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
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
    //añadir historial
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

    fun getAllHistorial(): List<Historial> {
        val historialList = mutableListOf<Historial>()
        val db = this.readableDatabase
        val cursor: Cursor

        try {
            cursor = db.query("Historial", null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val idHistorial = cursor.getInt(cursor.getColumnIndexOrThrow("idHistorial"))
                    val aula = cursor.getInt(cursor.getColumnIndexOrThrow("aula"))
                    val categoria = cursor.getInt(cursor.getColumnIndexOrThrow("categoria"))
                    val componente = cursor.getInt(cursor.getColumnIndexOrThrow("componente"))
                    val fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                    val hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"))
                    val status = cursor.getInt(cursor.getColumnIndexOrThrow("status"))

                    val historial = Historial(idHistorial, aula, categoria, componente, fecha, hora, status)
                    historialList.add(historial)
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
}
