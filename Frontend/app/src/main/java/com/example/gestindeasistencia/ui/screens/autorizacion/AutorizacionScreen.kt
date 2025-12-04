package com.example.gestindeasistencia.ui.screens.autorizacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestindeasistencia.data.models.AutorizacionDto
import com.example.gestindeasistencia.viewmodels.AutorizacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutorizacionScreen(
    viewModel: AutorizacionViewModel,
    userCargo: String,
    onBack: () -> Unit,
    onNuevaSolicitud: () -> Unit
) {
    val lista by viewModel.lista.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    // SnackBar para mensajes
    val snackState = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let { snackState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Permisos y Salidas", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            // Solo empleados normales o admins pueden solicitar (todos)
            FloatingActionButton(onClick = onNuevaSolicitud, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Solicitud", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (lista.isEmpty()) {
                    Text("No hay solicitudes registradas", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lista) { auth ->
                            AutorizacionItem(auth, userCargo,
                                onAprobar = { viewModel.aprobar(it) },
                                onRechazar = { viewModel.rechazar(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AutorizacionItem(
    auth: AutorizacionDto,
    userCargo: String,
    onAprobar: (Int) -> Unit,
    onRechazar: (Int) -> Unit
) {
    val isAdmin = userCargo == "Administrador de Sistemas"
    val colorEstado = when(auth.estado) {
        "APROBADO" -> Color(0xFF4CAF50) // Verde
        "RECHAZADO" -> Color(0xFFF44336) // Rojo
        else -> Color(0xFFFF9800) // Naranja (Pendiente)
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colorEstado, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = auth.estado ?: "PENDIENTE",
                    color = colorEstado,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = auth.fechaSolicitud?.replace("T", " ")?.take(16) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Si es admin, mostrar quién solicita
            if (isAdmin) {
                Text(
                    text = "Solicitante: ${auth.usuarioSolicita?.usuario ?: "Desconocido"}",
                    fontWeight = FontWeight.Bold
                )
            }

            Text("Motivo: ${auth.movimiento?.descripcion ?: "General"}")
            if (!auth.descripcion.isNullOrBlank()) {
                Text("Detalle: ${auth.descripcion}", style = MaterialTheme.typography.bodyMedium)
            }

            // Botones de acción solo para ADMIN y si está PENDIENTE
            if (isAdmin && auth.estado == "PENDIENTE" && auth.idAutorizacion != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = { onRechazar(auth.idAutorizacion) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAprobar(auth.idAutorizacion) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar")
                    }
                }
            }
        }
    }
}