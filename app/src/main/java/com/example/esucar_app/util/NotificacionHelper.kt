package com.example.esucar_app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificacionHelper {

    private const val CHANNEL_ID = "canal_hitos_esucar"
    private const val CHANNEL_NAME = "Recordatorios de Trabajo de Título"

    fun crearCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importancia).apply {
                description = "Alertas de fechas límite de entregas académicas ESUCAR"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    fun mostrarNotificacionHito(context: Context, tituloHito: String, fechaLimite: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Recordatorio Académico ESUCAR")
            .setContentText("Entrega programada: $tituloHito (Vence: $fechaLimite)")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Recuerda revisar la rúbrica y el formato APA 7 para el hito '$tituloHito'. Fecha límite de entrega: $fechaLimite."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(tituloHito.hashCode(), builder.build())
    }
}