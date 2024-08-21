import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.proyectofinal.database.Componente
import com.example.proyectofinal.database.Historial
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class FirebaseWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("RFID")

        myRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val rfid = snapshot.key // Obtener el RFID que es la clave del snapshot

                if (rfid != null) {
                    sendNotificationAndRecordHistory(rfid)
                } else {
                    Log.e("FirebaseWorker", "RFID es nulo")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        return Result.success()
    }

    private fun sendNotificationAndRecordHistory(rfid: String) {
        // 1. Enviar notificación
        sendNotification(rfid)

        // 2. Registrar historial en Firebase
        val databaseReference = FirebaseDatabase.getInstance().getReference("RFID/$rfid")
        databaseReference.get().addOnSuccessListener { snapshot ->
            val componentId = snapshot.getValue(String::class.java)
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

                        val historialRef = FirebaseDatabase.getInstance().getReference("Historial")
                        historialRef.push().setValue(historial)
                            .addOnSuccessListener {
                                Log.d("FirebaseWorker", "Historial registrado correctamente en Firebase")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("FirebaseWorker", "Error al registrar el historial en Firebase: ${exception.message}")
                            }
                    } else {
                        Log.e("FirebaseWorker", "Componente no encontrado en la base de datos.")
                    }
                }
            } else {
                Log.e("FirebaseWorker", "ID del componente no encontrado en la base de datos.")
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseWorker", "Error al obtener el componente ID: ${exception.message}")
        }
    }

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

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "RFID_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_notification_overlay) // Usa un ícono predeterminado para pruebas
            .setContentTitle("Componente Detectado")
            .setContentText("ID: $cardId")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun getComponenteByIdFirebase(id: String, callback: (Componente?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("componentes")
        databaseReference.child(id).get().addOnSuccessListener { snapshot ->
            val componente = snapshot.getValue(Componente::class.java)
            callback(componente)
        }.addOnFailureListener { exception ->
            Log.e("FirebaseWorker", "Error al obtener el componente por ID: ${exception.message}")
            callback(null)
        }
    }
}
