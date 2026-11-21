package com.example.esucar_app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.esucar_app.MainActivity
import com.example.esucar_app.R

object NotificacionHelper {

    private const val CHANNEL_ID = "canal_hitos_esucar"
    private const val CHANNEL_NAME = "Recordatorios de Trabajo de Título"

    fun crearCanalNotificaciones(context: Context) {
        val importancia = NotificationManager.IMPORTANCE_HIGH
        val canal = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importancia).apply {
            description = "Alertas de fechas límite de entregas académicas ESUCAR"
            enableVibration(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(canal)
    }

    fun tienePermisoNotificaciones(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun mostrarNotificacionHito(
        context: Context,
        tituloHito: String,
        fechaLimite: String,
        notificationId: Int = tituloHito.trim().hashCode(),
        mensajeExtra: String? = null
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val tituloLimpio = tituloHito.trim()
        val fechaLimpia = fechaLimite.trim()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cuerpoExtendido = buildString {
            append("Recuerda revisar la rúbrica y el formato APA 7 para el hito '$tituloLimpio'. ")
            append("Fecha límite de entrega: $fechaLimpia.")
            if (!mensajeExtra.isNullOrBlank()) {
                append(" $mensajeExtra")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notificacion_esucar)
            .setContentTitle("Recordatorio Académico ESUCAR")
            .setContentText("Entrega: $tituloLimpio (Vence: $fechaLimpia)")
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpoExtendido))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        return true
    }
}