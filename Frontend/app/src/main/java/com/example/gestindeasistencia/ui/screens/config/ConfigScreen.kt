package com.example.gestindeasistencia.ui.screens.config

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.gestindeasistencia.data.remote.ApiClient
import com.example.gestindeasistencia.data.repositorio.PersonalRepository
import com.example.gestindeasistencia.utils.BiometricHelper
import com.example.gestindeasistencia.utils.JwtUtils
import com.example.gestindeasistencia.utils.SecurePrefs
import com.example.gestindeasistencia.utils.NotificationHelper
import com.example.gestindeasistencia.utils.ReminderScheduler
import com.example.gestindeasistencia.utils.SettingsPrefs
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    userName: String,
    userCargo: String,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Estados de configuración
    var darkTheme by remember { mutableStateOf(SettingsPrefs.isDarkTheme(context)) }
    var notificationEntrada by remember { mutableStateOf(SettingsPrefs.isNotificationEntradaEnabled(context)) }
    var notificationSalida by remember { mutableStateOf(SettingsPrefs.isNotificationSalidaEnabled(context)) }
    var horaEntrada by remember { mutableStateOf(SettingsPrefs.getHoraEntrada(context)) }
    var horaSalida by remember { mutableStateOf(SettingsPrefs.getHoraSalida(context)) }
    
    // Diálogos
    var showAboutDialog by remember { mutableStateOf(false) }
    var showBiometricUnavailableDialog by remember { mutableStateOf(false) }
    
    // Verificar disponibilidad biométrica
    val biometricHelper = remember { BiometricHelper(context) }
    val isBiometricAvailable = remember { biometricHelper.isBiometricAvailable() }
    
    // Permiso de notificaciones (Android 13+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "✓ Notificaciones habilitadas", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "⚠️ Sin permiso de notificaciones", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Crear canal de notificaciones al cargar
    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Configuración",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Obtener foto y nombre personalizados (se lee cada vez que se renderiza para que se actualice)
            val profileImageUri = SettingsPrefs.getProfileImageUri(context)
            var nombreBackend by remember { mutableStateOf<String?>(null) }
            
            // Cargar nombre del Personal desde el backend
            LaunchedEffect(Unit) {
                val token = SecurePrefs.getToken(context)
                if (token != null) {
                    val userId = JwtUtils.extractId(token)
                    if (userId != null) {
                        try {
                            val api = ApiClient.getClient(context)
                            val usuarioResponse = api.obtenerUsuario(userId)
                            if (usuarioResponse.isSuccessful && usuarioResponse.body() != null) {
                                val usuario = usuarioResponse.body()!!
                                val personalId = usuario.personal?.idPersonal
                                
                                if (personalId != null) {
                                    val personalRepo = PersonalRepository(context)
                                    val personalResult = personalRepo.obtenerPersonal(personalId)
                                    
                                    if (personalResult.isSuccess) {
                                        val personal = personalResult.getOrNull()
                                        if (personal != null) {
                                            // Construir nombre completo desde el backend
                                            val nombreCompleto = listOfNotNull(
                                                personal.nombre,
                                                personal.apellPaterno,
                                                personal.apellMaterno
                                            ).joinToString(" ").trim()
                                            
                                            if (nombreCompleto.isNotEmpty()) {
                                                nombreBackend = nombreCompleto
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Si falla, usar datos locales
                        }
                    }
                }
            }
            
            // Leer nombre personalizado o del backend (se actualiza cada vez que se renderiza)
            val displayName = SettingsPrefs.getCustomName(context) ?: nombreBackend ?: userName
            
            // ===== HEADER CON PERFIL =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditProfile() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar con foto o inicial
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = displayName.firstOrNull()?.uppercase() ?: "U",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = userCargo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Toca para editar perfil",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ===== SECCIÓN: CUENTA =====
            SettingsSection(title = "Cuenta") {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Editar perfil",
                    subtitle = "Foto, nombre, email",
                    onClick = onEditProfile
                )
                
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Cambiar contraseña",
                    subtitle = "Actualiza tu contraseña de acceso",
                    onClick = onChangePassword
                )
            }

            // ===== SECCIÓN: SEGURIDAD =====
            SettingsSection(title = "Seguridad") {
                // Huella es OBLIGATORIA - solo mostrar estado, no toggle
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Autenticación biométrica",
                    subtitle = if (isBiometricAvailable) 
                        "✓ Disponible - OBLIGATORIA para marcar asistencia" 
                    else 
                        "⚠️ No disponible - No podrás marcar asistencia",
                    onClick = {
                        if (isBiometricAvailable) {
                            Toast.makeText(
                                context,
                                "La huella es obligatoria para marcar asistencia. Esto garantiza que solo tú puedas marcar.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            showBiometricUnavailableDialog = true
                        }
                    }
                )
            }

            // ===== SECCIÓN: NOTIFICACIONES =====
            SettingsSection(title = "Notificaciones") {
                // Mostrar estado del permiso en Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    SettingsItem(
                        icon = Icons.Default.NotificationsOff,
                        title = "Permiso de notificaciones",
                        subtitle = "⚠️ Toca para habilitar notificaciones",
                        onClick = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                SettingsToggleItem(
                    icon = Icons.Default.NotificationsActive,
                    title = "Recordatorio de entrada",
                    subtitle = if (notificationEntrada) "Activo - Te recordaremos a las $horaEntrada" else "Desactivado",
                    checked = notificationEntrada,
                    onCheckedChange = { enabled ->
                        // Verificar permiso primero
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            return@SettingsToggleItem
                        }
                        
                        notificationEntrada = enabled
                        SettingsPrefs.setNotificationEntradaEnabled(context, enabled)
                        
                        if (enabled) {
                            ReminderScheduler.scheduleEntradaReminder(context)
                            Toast.makeText(context, "✓ Recordatorio de entrada activado para las $horaEntrada", Toast.LENGTH_SHORT).show()
                        } else {
                            ReminderScheduler.cancelEntradaReminder(context)
                            Toast.makeText(context, "Recordatorio de entrada desactivado", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                
                if (notificationEntrada) {
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "Hora de recordatorio",
                        subtitle = horaEntrada,
                        onClick = {
                            showTimePicker(context, horaEntrada) { newHora ->
                                horaEntrada = newHora
                                SettingsPrefs.setHoraEntrada(context, newHora)
                                // Reprogramar con la nueva hora
                                ReminderScheduler.scheduleEntradaReminder(context)
                                Toast.makeText(context, "✓ Hora actualizada: $newHora", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SettingsToggleItem(
                    icon = Icons.Default.NotificationsActive,
                    title = "Recordatorio de salida",
                    subtitle = if (notificationSalida) "Activo - Te recordaremos a las $horaSalida" else "Desactivado",
                    checked = notificationSalida,
                    onCheckedChange = { enabled ->
                        // Verificar permiso primero
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            return@SettingsToggleItem
                        }
                        
                        notificationSalida = enabled
                        SettingsPrefs.setNotificationSalidaEnabled(context, enabled)
                        
                        if (enabled) {
                            ReminderScheduler.scheduleSalidaReminder(context)
                            Toast.makeText(context, "✓ Recordatorio de salida activado para las $horaSalida", Toast.LENGTH_SHORT).show()
                        } else {
                            ReminderScheduler.cancelSalidaReminder(context)
                            Toast.makeText(context, "Recordatorio de salida desactivado", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                
                if (notificationSalida) {
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "Hora de recordatorio",
                        subtitle = horaSalida,
                        onClick = {
                            showTimePicker(context, horaSalida) { newHora ->
                                horaSalida = newHora
                                SettingsPrefs.setHoraSalida(context, newHora)
                                // Reprogramar con la nueva hora
                                ReminderScheduler.scheduleSalidaReminder(context)
                                Toast.makeText(context, "✓ Hora actualizada: $newHora", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // ===== SECCIÓN: APARIENCIA =====
            SettingsSection(title = "Apariencia") {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Tema oscuro",
                    subtitle = if (darkTheme) "Activado" else "Desactivado",
                    checked = darkTheme,
                    onCheckedChange = { enabled ->
                        darkTheme = enabled
                        SettingsPrefs.setDarkTheme(context, enabled)
                        onDarkThemeChanged(enabled)
                    }
                )
            }

            // ===== SECCIÓN: INFORMACIÓN =====
            SettingsSection(title = "Información") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Acerca de",
                    subtitle = "Versión 1.0.0",
                    onClick = { showAboutDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Términos y condiciones",
                    subtitle = "Lee nuestros términos de uso",
                    onClick = {
                        Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Política de privacidad",
                    subtitle = "Cómo manejamos tus datos",
                    onClick = {
                        Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.HelpOutline,
                    title = "Soporte",
                    subtitle = "¿Necesitas ayuda?",
                    onClick = {
                        Toast.makeText(context, "Contacta a: soporte@gestiais.com", Toast.LENGTH_LONG).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ===== DIÁLOGO: ACERCA DE =====
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "GestiAsis",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sistema de Gestión de Asistencia")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Versión 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Desarrollado por:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Equipo de Desarrollo Móvil UTP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "© 2025 Todos los derechos reservados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // ===== DIÁLOGO: BIOMETRÍA NO DISPONIBLE =====
    if (showBiometricUnavailableDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricUnavailableDialog = false },
            icon = {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Biometría no disponible") },
            text = {
                Text(BiometricHelper.getBiometricStatusMessage(context))
            },
            confirmButton = {
                TextButton(onClick = { showBiometricUnavailableDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

// ===== COMPONENTES AUXILIARES =====

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                content = content
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

// ===== FUNCIONES AUXILIARES =====

private fun showTimePicker(
    context: android.content.Context,
    currentTime: String,
    onTimeSelected: (String) -> Unit
) {
    val parts = currentTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 50
    
    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        },
        hour,
        minute,
        true // 24 hour format
    ).show()
}
