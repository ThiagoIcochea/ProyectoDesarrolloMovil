package com.example.gestindeasistencia.ui.screens.config

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.gestindeasistencia.data.models.LoginRequest
import com.example.gestindeasistencia.data.models.UsuarioDto
import com.example.gestindeasistencia.data.remote.ApiClient
import com.example.gestindeasistencia.utils.BiometricHelper
import com.example.gestindeasistencia.utils.DeviceHelper
import com.example.gestindeasistencia.utils.JwtUtils
import com.example.gestindeasistencia.utils.SecurePrefs
import com.example.gestindeasistencia.utils.SettingsPrefs
import com.example.gestindeasistencia.BuildConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onPasswordChanged: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val biometricHelper = remember { BiometricHelper(context) }
    val isBiometricAvailable = remember { biometricHelper.isBiometricAvailable() }
    val isEmulator = remember { DeviceHelper.isEmulator() }
    
    val token = remember { SecurePrefs.getToken(context) }
    val userId = remember { token?.let { JwtUtils.extractId(it) } }
    val username = remember { token?.let { JwtUtils.getUserName(it) } }
    
    val apiService = remember { ApiClient.getClient(context) }
    
    val isCurrentPasswordValid = currentPassword.length >= 6
    val isNewPasswordValid = newPassword.length >= 6
    val doPasswordsMatch = newPassword == confirmPassword && confirmPassword.isNotEmpty()
    val isFormValid = isCurrentPasswordValid && isNewPasswordValid && doPasswordsMatch

    fun cambiarPassword() {
        if (userId == null || username == null) {
            errorMessage = "Error: No se pudo obtener información del usuario"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        scope.launch {
            try {
                val loginResponse = apiService.login(LoginRequest(username, currentPassword))
                
                if (!loginResponse.isSuccessful) {
                    isLoading = false
                    errorMessage = "La contraseña actual es incorrecta"
                    return@launch
                }
                
                val usuarioActualizado = UsuarioDto(
                    idUsuario = userId,
                    rol = null,
                    personal = null,
                    usuario = username,
                    password = newPassword,
                    estado = "ACTIVO",
                    fechaCreacion = null
                )
                
                val updateResponse = apiService.actualizarUsuario(userId, usuarioActualizado)
                
                isLoading = false
                
                if (updateResponse.isSuccessful) {
                    Toast.makeText(
                        context,
                        "✓ Contraseña actualizada correctamente",
                        Toast.LENGTH_LONG
                    ).show()
                    onPasswordChanged()
                } else {
                    errorMessage = "Error al actualizar: ${updateResponse.code()}"
                }
                
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error de conexión: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Cambiar Contraseña",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.padding(vertical = 24.dp),
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDE8))
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Text(
                text = "Actualiza tu contraseña",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Por seguridad, te recomendamos usar una contraseña única",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { 
                    currentPassword = it
                    errorMessage = null
                },
                label = { Text("Contraseña actual") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            if (showCurrentPassword) Icons.Default.Visibility 
                            else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                visualTransformation = if (showCurrentPassword) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = currentPassword.isNotEmpty() && !isCurrentPasswordValid,
                supportingText = if (currentPassword.isNotEmpty() && !isCurrentPasswordValid) {
                    { Text("Mínimo 6 caracteres") }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    errorMessage = null
                },
                label = { Text("Nueva contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            if (showNewPassword) Icons.Default.Visibility 
                            else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                visualTransformation = if (showNewPassword) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = newPassword.isNotEmpty() && !isNewPasswordValid,
                supportingText = if (newPassword.isNotEmpty() && !isNewPasswordValid) {
                    { Text("Mínimo 6 caracteres") }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirmar nueva contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Default.Visibility 
                            else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = confirmPassword.isNotEmpty() && !doPasswordsMatch,
                supportingText = if (confirmPassword.isNotEmpty() && !doPasswordsMatch) {
                    { Text("Las contraseñas no coinciden") }
                } else null
            )

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Requisitos de contraseña:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    RequirementItem(
                        text = "Mínimo 6 caracteres",
                        isMet = isNewPasswordValid
                    )
                    RequirementItem(
                        text = "Las contraseñas coinciden",
                        isMet = doPasswordsMatch
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isEmulator && BuildConfig.DEBUG) {
                        cambiarPassword()
                        return@Button
                    }
                    
                    if (!isBiometricAvailable) {
                        errorMessage = "⚠️ HUELLA OBLIGATORIA\n\nNo se puede verificar la huella. No puedes cambiar la contraseña."
                        return@Button
                    }
                    
                    if (context is FragmentActivity) {
                        biometricHelper.authenticate(
                            activity = context,
                            title = "Confirmar identidad",
                            subtitle = "Usa tu huella para confirmar el cambio de contraseña",
                            onSuccess = {
                                cambiarPassword()
                            },
                            onError = { error ->
                                errorMessage = "Autenticación fallida: $error"
                            }
                        )
                    } else {
                        errorMessage = "⚠️ No se puede verificar la huella en este dispositivo"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        if (isBiometricAvailable && !(isEmulator && BuildConfig.DEBUG)) 
                            Icons.Default.Fingerprint 
                        else 
                            Icons.Default.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBiometricAvailable && !(isEmulator && BuildConfig.DEBUG)) 
                            "Cambiar con huella" 
                        else 
                            "Cambiar contraseña",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isBiometricAvailable && !(isEmulator && BuildConfig.DEBUG)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Se requerirá verificación biométrica obligatoria",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isEmulator && BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚠️ MODO PRUEBA: Sin huella (solo desarrollo)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cambiando contraseña para: $username",
                        color = Color(0xFF1976D2),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RequirementItem(
    text: String,
    isMet: Boolean
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMet) Color(0xFF4CAF50) else Color.Gray
        )
    }
}
