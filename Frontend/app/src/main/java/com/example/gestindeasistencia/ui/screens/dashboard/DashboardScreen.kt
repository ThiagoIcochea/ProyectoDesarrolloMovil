package com.example.gestindeasistencia.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gestindeasistencia.data.remote.ApiClient
import com.example.gestindeasistencia.data.repositorio.PersonalRepository
import com.example.gestindeasistencia.utils.JwtUtils
import com.example.gestindeasistencia.utils.SecurePrefs
import com.example.gestindeasistencia.utils.SettingsPrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    userCargo: String,
    onGestionPersonal: () -> Unit,
    onReportes: () -> Unit,
    onPerfil: () -> Unit,
    onConfig: () -> Unit,
    onAsistencia: () -> Unit = {},
    onAutorizaciones: () -> Unit = {},
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "GestiAsis - Panel Principal",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val context = LocalContext.current
        
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar con foto o inicial
                    Box(
                        modifier = Modifier
                            .size(60.dp)
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
                                text = displayName.first().uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
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
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // --- accesos ---

            DashboardCard(
                title = "Registrar asistencia",
                icon = Icons.Filled.AssignmentTurnedIn,
                onClick = onAsistencia
            )

            DashboardCard(
                title = "Permisos y Salidas",
                icon = Icons.Filled.ExitToApp,
                onClick = onAutorizaciones
            )

            // Mostrar 'Reportes' solo a administradores
            if (userCargo == "Administrador de Sistemas") {
                DashboardCard(
                    title = "Reportes",
                    icon = Icons.Outlined.Assessment,
                    onClick = onReportes
                )
            }

            DashboardCard(
                title = "Mi Perfil",
                icon = Icons.Filled.Person,
                onClick = onPerfil
            )

            DashboardCard(
                title = "Configuración",
                icon = Icons.Filled.Settings,
                onClick = onConfig
            )




            //acceso para el administrador
            if (userCargo == "Administrador de Sistemas") {
                Spacer(Modifier.height(24.dp))

                Text(
                    "Opciones de Administración", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                DashboardCard(
                    title = "Gestión de Personal",
                    icon = Icons.Filled.ManageAccounts,
                    onClick = onGestionPersonal
                )

                // El apartado "Gestión de Documentos" eliminado: ya no estará disponible
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
