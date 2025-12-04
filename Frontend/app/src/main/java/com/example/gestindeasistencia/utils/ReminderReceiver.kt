package com.example.gestindeasistencia.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver que recibe las alarmas programadas y muestra las notificaciones
 */
class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_ENTRADA_REMINDER = "com.example.gestindeasistencia.ENTRADA_REMINDER"
        const val ACTION_SALIDA_REMINDER = "com.example.gestindeasistencia.SALIDA_REMINDER"
        const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Received action: ${intent.action}")
        
        when (intent.action) {
            ACTION_ENTRADA_REMINDER -> {
                Log.d("ReminderReceiver", "Showing entrada reminder")
                NotificationHelper.showEntradaReminder(context)
                // Reprogramar para el siguiente día
                ReminderScheduler.scheduleEntradaReminder(context)
            }
            
            ACTION_SALIDA_REMINDER -> {
                Log.d("ReminderReceiver", "Showing salida reminder")
                NotificationHelper.showSalidaReminder(context)
                // Reprogramar para el siguiente día
                ReminderScheduler.scheduleSalidaReminder(context)
            }
            
            ACTION_BOOT_COMPLETED -> {
                // Cuando el dispositivo reinicia, reprogramar las alarmas si están habilitadas
                Log.d("ReminderReceiver", "Boot completed, rescheduling alarms")
                ReminderScheduler.rescheduleAllReminders(context)
            }
        }
    }
}

