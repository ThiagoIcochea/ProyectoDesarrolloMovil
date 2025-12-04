package com.example.gestindeasistencia.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

/**
 * Programador de recordatorios usando AlarmManager
 */
object ReminderScheduler {
    
    private const val REQUEST_CODE_ENTRADA = 2001
    private const val REQUEST_CODE_SALIDA = 2002
    
    /**
     * Programa el recordatorio de entrada
     */
    fun scheduleEntradaReminder(context: Context) {
        if (!SettingsPrefs.isNotificationEntradaEnabled(context)) {
            Log.d("ReminderScheduler", "Entrada reminder disabled, not scheduling")
            return
        }
        
        val horaStr = SettingsPrefs.getHoraEntrada(context) // formato "HH:mm"
        val parts = horaStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 50
        
        scheduleReminder(
            context = context,
            requestCode = REQUEST_CODE_ENTRADA,
            action = ReminderReceiver.ACTION_ENTRADA_REMINDER,
            hour = hour,
            minute = minute
        )
        
        Log.d("ReminderScheduler", "Scheduled entrada reminder at $hour:$minute")
    }
    
    /**
     * Programa el recordatorio de salida
     */
    fun scheduleSalidaReminder(context: Context) {
        if (!SettingsPrefs.isNotificationSalidaEnabled(context)) {
            Log.d("ReminderScheduler", "Salida reminder disabled, not scheduling")
            return
        }
        
        val horaStr = SettingsPrefs.getHoraSalida(context) // formato "HH:mm"
        val parts = horaStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 17
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 50
        
        scheduleReminder(
            context = context,
            requestCode = REQUEST_CODE_SALIDA,
            action = ReminderReceiver.ACTION_SALIDA_REMINDER,
            hour = hour,
            minute = minute
        )
        
        Log.d("ReminderScheduler", "Scheduled salida reminder at $hour:$minute")
    }
    
    /**
     * Cancela el recordatorio de entrada
     */
    fun cancelEntradaReminder(context: Context) {
        cancelReminder(context, REQUEST_CODE_ENTRADA, ReminderReceiver.ACTION_ENTRADA_REMINDER)
        Log.d("ReminderScheduler", "Cancelled entrada reminder")
    }
    
    /**
     * Cancela el recordatorio de salida
     */
    fun cancelSalidaReminder(context: Context) {
        cancelReminder(context, REQUEST_CODE_SALIDA, ReminderReceiver.ACTION_SALIDA_REMINDER)
        Log.d("ReminderScheduler", "Cancelled salida reminder")
    }
    
    /**
     * Reprograma todos los recordatorios (usado después de reiniciar el dispositivo)
     */
    fun rescheduleAllReminders(context: Context) {
        if (SettingsPrefs.isNotificationEntradaEnabled(context)) {
            scheduleEntradaReminder(context)
        }
        if (SettingsPrefs.isNotificationSalidaEnabled(context)) {
            scheduleSalidaReminder(context)
        }
    }
    
    /**
     * Programa una alarma para una hora específica
     */
    private fun scheduleReminder(
        context: Context,
        requestCode: Int,
        action: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = action
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calcular el tiempo para la próxima alarma
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Si ya pasó la hora hoy, programar para mañana
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        try {
            // Usar setExactAndAllowWhileIdle para mejor precisión
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            
            Log.d("ReminderScheduler", "Alarm scheduled for ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "No permission to schedule exact alarm: ${e.message}")
            // Fallback a alarma inexacta
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancela una alarma programada
     */
    private fun cancelReminder(context: Context, requestCode: Int, action: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = action
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}

