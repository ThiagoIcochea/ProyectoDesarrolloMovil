package com.example.gestindeasistencia.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver que se ejecuta cuando el dispositivo termina de iniciar.
 * Reprograma las alarmas de recordatorio.
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device boot completed, rescheduling reminders")
            
            // Reprogramar todas las alarmas habilitadas
            ReminderScheduler.rescheduleAllReminders(context)
        }
    }
}

