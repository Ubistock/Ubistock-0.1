package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button

class Menu_main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu_main)

        // Manejar los insets para asegurar que el contenido no se solape con la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar los botones
        val historialButton: Button = findViewById(R.id.historialButton)
        val anadirAulaButton: Button = findViewById(R.id.anadirAulaButton)
        val anadirCategoriaButton: Button = findViewById(R.id.anadirCategoriaButton)
        val anadirPropietarioButton: Button = findViewById(R.id.anadirPropietarioButton)
        val anadirPuestoButton: Button = findViewById(R.id.anadirPuestoButton)

        // Configurar acciones de los botones
        historialButton.setOnClickListener {
            // Navegar a la actividad de Historial
            val intent = Intent(this, Historial::class.java)
            startActivity(intent)
        }

        anadirAulaButton.setOnClickListener {
            // Navegar a la actividad para añadir Aula
            val intent = Intent(this, Add_Aula::class.java)
            startActivity(intent)
        }

        anadirCategoriaButton.setOnClickListener {
            // Navegar a la actividad para añadir Categoría
            val intent = Intent(this, Add_Categoria::class.java)
            startActivity(intent)
        }

        anadirPropietarioButton.setOnClickListener {
            // Navegar a la actividad para añadir Propietario
            val intent = Intent(this, Add_Propietario::class.java)
            startActivity(intent)
        }

        anadirPuestoButton.setOnClickListener {
            // Navegar a la actividad para añadir Puesto
            val intent = Intent(this, Add_Puesto::class.java)
            startActivity(intent)
        }
    }
}
