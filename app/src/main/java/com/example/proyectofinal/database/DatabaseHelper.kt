package com.example.proyectofinal.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

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
 
}
