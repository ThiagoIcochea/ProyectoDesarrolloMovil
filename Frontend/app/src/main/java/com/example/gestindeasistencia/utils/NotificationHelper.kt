package com.example.gestindeasistencia.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gestindeasistencia.MainActivity
import com.example.gestindeasistencia.R

object NotificationHelper {
    
    const val CHANNEL_ID = "reminder_channel"
    const val CHANNEL_NAME = "Recordatorios de Asistencia"
    const val CHANNEL_DESCRIPTION = "Notificaciones para recordar marcar entrada y salida"
    
    const val NOTIFICATION_ID_ENTRADA = 1001
    const val NOTIFICATION_ID_SALIDA = 1002
    
    /**
     * Crea el canal de notificaciones (requerido para Android 8+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Muestra la notificación de recordatorio de entrada
     */
    fun showEntradaReminder(context: Context) {
        showReminder(
            context = context,
            notificationId = NOTIFICATION_ID_ENTRADA,
            title = "⏰ Hora de marcar entrada",
            message = "Recuerda registrar tu asistencia de entrada en GestiAsis"
        )
    }
    
    /**
     * Muestra la notificación de recordatorio de salida
     */
    fun showSalidaReminder(context: Context) {
        showReminder(
            context = context,
            notificationId = NOTIFICATION_ID_SALIDA,
            title = "⏰ Hora de marcar salida",
            message = "Recuerda registrar tu asistencia de salida en GestiAsis"
        )
    }
    
    /**
     * Muestra una notificación de recordatorio
     */
    private fun showReminder(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        // Crear el canal si no existe
        createNotificationChannel(context)
        
        // Intent para abrir la app al tocar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        // Mostrar la notificación
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permiso de notificación no concedido
            e.printStackTrace()
        }
    }
    
    /**
     * Cancela todas las notificaciones
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}

