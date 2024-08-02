package com.example.proyectofinal.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "miInventario.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE Aula (
                idLab INTEGER PRIMARY KEY AUTOINCREMENT,
                edificio TEXT,
                nombre TEXT,
                status INTEGER
            )
        """)
        // Crear otras tablas si es necesario
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Aula")
        // Eliminar otras tablas si es necesario
        onCreate(db)
    }
}

