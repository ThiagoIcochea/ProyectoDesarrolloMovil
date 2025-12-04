package com.example.gestindeasistencia.ui.screens.config

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gestindeasistencia.data.models.PersonalDto
import com.example.gestindeasistencia.data.remote.ApiClient
import com.example.gestindeasistencia.data.repositorio.PersonalRepository
import com.example.gestindeasistencia.utils.BiometricHelper
import com.example.gestindeasistencia.utils.DeviceHelper
import com.example.gestindeasistencia.utils.JwtUtils
import com.example.gestindeasistencia.utils.SecurePrefs
import com.example.gestindeasistencia.utils.SettingsPrefs
import com.example.gestindeasistencia.BuildConfig
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userName: String,
    userEmail: String,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Estados
    var nombre by remember { 
        mutableStateOf(SettingsPrefs.getCustomName(context) ?: userName) 
    }
    var email by remember { 
        mutableStateOf(SettingsPrefs.getCustomEmail(context) ?: userEmail) 
    }
    var telefono by remember { 
        mutableStateOf(SettingsPrefs.getCustomPhone(context) ?: "") 
    }
    var profileImageUri by remember { 
        mutableStateOf(SettingsPrefs.getProfileImageUri(context)) 
    }
    var isLoading by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }
    var personalActual by remember { mutableStateOf<PersonalDto?>(null) }
    
    // Helper de biometría
    val biometricHelper = remember { BiometricHelper(context) }
    val isBiometricAvailable = remember { biometricHelper.isBiometricAvailable() }
    val isEmulator = remember { DeviceHelper.isEmulator() }
    
    // Cargar datos del personal desde el backend
    LaunchedEffect(Unit) {
        val token = SecurePrefs.getToken(context)
        if (token != null) {
            val userId = JwtUtils.extractId(token)
            if (userId != null) {
                try {
                    // Obtener usuario para saber el personalId
                    val api = ApiClient.getClient(context)
                    val usuarioResponse = api.obtenerUsuario(userId)
                    if (usuarioResponse.isSuccessful && usuarioResponse.body() != null) {
                        val usuario = usuarioResponse.body()!!
                        val personalId = usuario.personal?.idPersonal
                        
                        if (personalId != null) {
                            // Obtener datos del personal
                            val personalRepo = PersonalRepository(context)
                            val personalResult = personalRepo.obtenerPersonal(personalId)
                            
                            if (personalResult.isSuccess) {
                                personalActual = personalResult.getOrNull()
                                val personal = personalActual
                                
                                // Cargar datos del backend si existen
                                if (personal != null) {
                                    nombre = SettingsPrefs.getCustomName(context) 
                                        ?: "${personal.nombre ?: ""} ${personal.apellPaterno ?: ""} ${personal.apellMaterno ?: ""}".trim()
                                        .takeIf { it.isNotEmpty() } ?: userName
                                    email = SettingsPrefs.getCustomEmail(context) 
                                        ?: personal.email ?: userEmail
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
    
    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it.toString()
            SettingsPrefs.setProfileImageUri(context, it.toString())
            Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Validaciones
    val isNombreValid = nombre.length >= 2
    val isEmailValid = email.contains("@") && email.contains(".")
    val isFormValid = isNombreValid && isEmailValid
    
    // Función interna para guardar (después de verificar huella)
    fun guardarPerfilReal() {
        if (isFormValid && personalActual != null) {
            isLoading = true
            scope.launch {
                try {
                    val personalId = personalActual!!.idPersonal
                    if (personalId != null) {
                        // Separar nombre completo en partes
                        val partesNombre = nombre.trim().split(" ")
                        val nuevoNombre = partesNombre.firstOrNull() ?: ""
                        val apellPaterno = partesNombre.getOrNull(1) ?: ""
                        val apellMaterno = partesNombre.drop(2).joinToString(" ") ?: ""
                        
                        // Crear DTO actualizado
                        val personalActualizado = PersonalDto(
                            idPersonal = personalId,
                            cargo = personalActual!!.cargo,
                            documento = personalActual!!.documento,
                            nombre = nuevoNombre,
                            apellPaterno = apellPaterno,
                            apellMaterno = apellMaterno,
                            nroDocumento = personalActual!!.nroDocumento,
                            fechaNacimiento = personalActual!!.fechaNacimiento,
                            fechaIngreso = personalActual!!.fechaIngreso,
                            email = email
                        )
                        
                        // Actualizar en el backend
                        val personalRepo = PersonalRepository(context)
                        val result = personalRepo.actualizarPersonal(personalId, personalActualizado)
                        
                        if (result.isSuccess) {
                            // Guardar también localmente para mostrar inmediatamente
                            SettingsPrefs.setCustomName(context, nombre)
                            SettingsPrefs.setCustomEmail(context, email)
                            SettingsPrefs.setCustomPhone(context, telefono)
                            
                            Toast.makeText(
                                context,
                                "✓ Perfil actualizado correctamente",
                                Toast.LENGTH_LONG
                            ).show()
                            onSaved()
                        } else {
                            Toast.makeText(
                                context,
                                "Error al actualizar: ${result.exceptionOrNull()?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    isLoading = false
                }
            }
        } else if (personalActual == null) {
            // Si no hay personal, solo guardar localmente
            SettingsPrefs.setCustomName(context, nombre)
            SettingsPrefs.setCustomEmail(context, email)
            SettingsPrefs.setCustomPhone(context, telefono)
            Toast.makeText(
                context,
                "✓ Perfil guardado localmente",
                Toast.LENGTH_SHORT
            ).show()
            onSaved()
        }
    }
    
    // Función para guardar (verifica huella primero)
    fun guardarPerfil() {
        if (!isFormValid) return
        
        // Si es emulador en DEBUG, no pedir huella
        if (isEmulator && BuildConfig.DEBUG) {
            guardarPerfilReal()
            return
        }
        
        // Si no hay biometría disponible, bloquear
        if (!isBiometricAvailable) {
            Toast.makeText(
                context,
                "⚠️ HUELLA OBLIGATORIA\n\nNo se puede verificar la huella. No puedes realizar cambios.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        // Pedir huella antes de guardar
        if (context is FragmentActivity) {
            biometricHelper.authenticate(
                activity = context,
                title = "Confirmar identidad",
                subtitle = "Usa tu huella para confirmar los cambios en tu perfil",
                onSuccess = {
                    guardarPerfilReal()
                },
                onError = { error ->
                    Toast.makeText(
                        context,
                        "Autenticación fallida: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            Toast.makeText(
                context,
                "⚠️ No se puede verificar la huella en este dispositivo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Editar Perfil",
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
                actions = {
                    TextButton(
                        onClick = { guardarPerfil() },
                        enabled = isFormValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Guardar",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // ===== FOTO DE PERFIL =====
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { showImageOptions = true },
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
                            text = nombre.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Botón de editar foto
                FloatingActionButton(
                    onClick = { showImageOptions = true },
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomEnd),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            TextButton(onClick = { showImageOptions = true }) {
                Text(
                    "Cambiar foto de perfil",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ===== FORMULARIO =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Campo: Nombre completo
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre completo") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = nombre.isNotEmpty() && !isNombreValid,
                        supportingText = if (nombre.isNotEmpty() && !isNombreValid) {
                            { Text("Mínimo 2 caracteres") }
                        } else null
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo: Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = email.isNotEmpty() && !isEmailValid,
                        supportingText = if (email.isNotEmpty() && !isEmailValid) {
                            { Text("Email inválido") }
                        } else null
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campo: Teléfono
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono (opcional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("+51 999 999 999") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ===== INFORMACIÓN ADICIONAL (solo lectura) =====
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información de cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    InfoRow(
                        icon = Icons.Default.Badge,
                        label = "Usuario",
                        value = userName
                    )
                    
                    InfoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Miembro desde",
                        value = "Enero 2025"
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Para cambiar el usuario, contacta al administrador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // ===== BOTÓN GUARDAR =====
            Button(
                onClick = { guardarPerfil() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cambios", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // ===== DIÁLOGO: OPCIONES DE IMAGEN =====
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            icon = {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Foto de perfil") },
            text = {
                Column {
                    // Opción: Seleccionar de galería
                    ListItem(
                        headlineContent = { Text("Seleccionar de galería") },
                        leadingContent = {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            showImageOptions = false
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                    
                    // Opción: Eliminar foto
                    if (profileImageUri != null) {
                        ListItem(
                            headlineContent = { 
                                Text("Eliminar foto", color = MaterialTheme.colorScheme.error) 
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            modifier = Modifier.clickable {
                                showImageOptions = false
                                profileImageUri = null
                                SettingsPrefs.setProfileImageUri(context, null)
                                Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
