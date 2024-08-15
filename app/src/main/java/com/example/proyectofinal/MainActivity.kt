package com.example.proyectofinal

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectofinal.database.*
import com.example.proyectofinal.database.DatabaseHelper
import com.example.proyectofinal.database.Historial
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.usuarioEditText)
        val passwordEditText = findViewById<EditText>(R.id.contrasenaEditText)
        val loginButton = findViewById<Button>(R.id.iniciarSesionButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                            // Sincronización de datos desde Firebase a SQLite
                            syncDataFromFirebaseToSQLite("Aula", "Aula", ::mapAulaToContentValues)
                            syncDataFromFirebaseToSQLite("Categoria", "Categoria", ::mapCategoriaToContentValues)
                            syncDataFromFirebaseToSQLite("Componente", "Componente", ::mapComponenteToContentValues)
                            syncDataFromFirebaseToSQLite("Propietario", "Propietario", ::mapPropietarioToContentValues)
                            syncDataFromFirebaseToSQLite("Puesto", "Puesto", ::mapPuestoToContentValues)
                            syncDataFromFirebaseToSQLite("Historial", "Historial", ::mapHistorialToContentValues)

                            // Iniciar la siguiente actividad
                            val intent = Intent(this, Menu_main::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Email no registrado, por favor registrese", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, llene todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        val registerTextView = findViewById<TextView>(R.id.crearCuentaTextView)
        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Configuración del inicio de sesión de Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<ImageButton>(R.id.googleLoginButton).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    // Manejo del resultado del inicio de sesión
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign-In failed
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful
                    // startActivity(Intent(this, NextActivity::class.java))
                    Toast.makeText(this, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Menu_main::class.java)
                    startActivity(intent)
                } else {
                    // Sign-in failed
                    Toast.makeText(this, "Error en la autenticación con Firebase", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private inline fun <reified T> syncDataFromFirebaseToSQLite(
        firebasePath: String,
        tableName: String,
        crossinline mapToContentValues: (T) -> ContentValues
    ) {
        val databaseReference = FirebaseDatabase.getInstance().getReference(firebasePath)
        val databaseHelper = DatabaseHelper(this)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(T::class.java)
                    if (item != null) {
                        val values = mapToContentValues(item)
                        databaseHelper.writableDatabase.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al sincronizar $tableName", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun mapAulaToContentValues(aula: Aula): ContentValues {
        return ContentValues().apply {
            put("idLab", aula.idLab)
            put("edificio", aula.edificio)
            put("nombre", aula.nombre)
            put("status", aula.status)
        }
    }

    private fun mapCategoriaToContentValues(categoria: Categoria): ContentValues {
        return ContentValues().apply {
            put("idCategoria", categoria.idCategoria)
            put("nombre", categoria.nombre)
            put("status", categoria.status)
        }
    }

    private fun mapComponenteToContentValues(componente: Componente): ContentValues {
        return ContentValues().apply {
            put("idComponente", componente.idComponente)
            put("categoria", componente.categoria)
            put("propi", componente.propi)
            put("aula", componente.aula)
            put("status", componente.status)
        }
    }

    private fun mapPropietarioToContentValues(propietario: Propietario): ContentValues {
        return ContentValues().apply {
            put("idPropietario", propietario.idPropietario)
            put("nombre", propietario.nombre)
            put("puesto", propietario.puesto)
            put("numero", propietario.numero)
            put("status", propietario.status)
        }
    }

    private fun mapPuestoToContentValues(puesto: Puesto): ContentValues {
        return ContentValues().apply {
            put("idRango", puesto.idRango)
            put("puesto", puesto.puesto)
            put("status", puesto.status)
        }
    }
    private fun mapHistorialToContentValues(historial: Historial): ContentValues {
        return ContentValues().apply {
            put("idHistorial", historial.idHistorial)
            put("aula", historial.aula)
            put("categoria", historial.categoria)
            put("componente", historial.componente)
            put("fecha", historial.fecha)
            put("hora", historial.hora)
            put("status", historial.status)
        }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
