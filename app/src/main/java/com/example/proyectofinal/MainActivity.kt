package com.example.proyectofinal

import FirebaseWorker
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.proyectofinal.database.*
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
    private lateinit var firebaseWorkRequest: WorkRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Verifica si las notificaciones están habilitadas y, si no, muestra la interfaz de configuración
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent)
            }
        }

        // Initialize WorkRequest
        firebaseWorkRequest = OneTimeWorkRequest.Builder(FirebaseWorker::class.java).build()
        WorkManager.getInstance(this).enqueue(firebaseWorkRequest)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Setup UI elements
        val emailEditText = findViewById<EditText>(R.id.usuarioEditText)
        val passwordEditText = findViewById<EditText>(R.id.contrasenaEditText)
        val loginButton = findViewById<Button>(R.id.iniciarSesionButton)
        val registerTextView = findViewById<TextView>(R.id.crearCuentaTextView)
        val googleLoginButton = findViewById<ImageButton>(R.id.googleLoginButton)

        // Set up login button listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Por favor, llene todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up register text listener
        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up Google login button listener
        googleLoginButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    // Firebase Authentication Methods
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Menu_main::class.java))
                } else {
                    Toast.makeText(this, "Email no registrado, por favor registrese", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Menu_main::class.java))
                } else {
                    Toast.makeText(this, "Error en la autenticación con Firebase", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar sesión con Google: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Firebase Database Methods
    private fun getComponentIdFromRFID(rfid: String, callback: (String?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("RFID")
        databaseReference.child(rfid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val componentId = dataSnapshot.getValue(String::class.java)
                callback(componentId)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                callback(null)
            }
        })
    }

    private fun getComponenteByIdFirebase(id: String, callback: (Componente?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("componentes")
        databaseReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val componente = dataSnapshot.getValue(Componente::class.java)
                callback(componente)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("DatabaseError", "Error: ${databaseError.message}")
                callback(null)
            }
        })
    }

    private fun onRFIDDetected(rfid: String) {
        getComponentIdFromRFID(rfid) { componentId ->
            if (componentId != null) {
                getComponenteByIdFirebase(componentId) { componente ->
                    if (componente != null) {
                        val currentTimeMillis = System.currentTimeMillis()
                        val fecha = android.text.format.DateFormat.format("yyyy-MM-dd", currentTimeMillis).toString()
                        val hora = android.text.format.DateFormat.format("HH:mm:ss", currentTimeMillis).toString()

                        val historial = Historial(
                            aula = componente.aula,
                            categoria = componente.categoria,
                            componente = componente.idComponente,
                            fecha = fecha,
                            hora = hora,
                            status = componente.status
                        )

                        val databaseReference = FirebaseDatabase.getInstance().getReference("Historial")
                        val newHistorialRef = databaseReference.push()
                        newHistorialRef.setValue(historial)
                            .addOnSuccessListener {
                                Log.d("Historial", "Historial registrado correctamente en Firebase")
                                Toast.makeText(this, "Componente detectado: ${componente.idComponente}", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Historial", "Error al registrar el historial en Firebase: ${exception.message}")
                                Toast.makeText(this, "Componente detectado error al registrar: ${componente.idComponente}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.e("ComponenteError", "Componente no encontrado en la base de datos.")
                        Toast.makeText(this, "Componente no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("RFIDError", "RFID no encontrado en la base de datos.")
                Toast.makeText(this, "RFID no encontrado en la base de datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun simulateRFIDDetection(rfid: String) {
        onRFIDDetected(rfid)
    }

    // Notification Methods
    private fun sendNotification(cardId: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "RFID_CHANNEL",
                "RFID Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, "RFID_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_notification_overlay) // Usa un ícono predeterminado para pruebas
            .setContentTitle("RFID Detected")
            .setContentText("RFID card ID: $cardId")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, notificationBuilder.build())
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}