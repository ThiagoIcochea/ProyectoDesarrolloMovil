package com.example.gestindeasistencia.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestindeasistencia.viewmodels.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioDetailScreen(
    idUsuario: Int,
    viewModel: UsuarioViewModel,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDeleted: () -> Unit
) {
    val detalle by viewModel.detalle.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(idUsuario) {
        viewModel.cargaUsuario(idUsuario)
    }

    if (saveSuccess) {
        LaunchedEffect(Unit) {
            viewModel.resetSaveSuccess()
            viewModel.cargaUsuario(idUsuario)
        }
    }

    // Diálogo de confirmación
    if (showDialog && detalle != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    imageVector = if (detalle?.estado == "ACTIVO")
                        Icons.Default.Block
                    else
                        Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (detalle?.estado == "ACTIVO")
                        MaterialTheme.colorScheme.error
                    else
                        Color(0xFF4CAF50)
                )
            },
            title = {
                Text(
                    if (detalle?.estado == "ACTIVO")
                        "¿Inhabilitar Usuario?"
                    else
                        "¿Habilitar Usuario?"
                )
            },
            text = {
                Text(
                    if (detalle?.estado == "ACTIVO")
                        "El usuario '${detalle?.usuario}' no podrá iniciar sesión en el sistema."
                    else
                        "El usuario '${detalle?.usuario}' podrá iniciar sesión nuevamente."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nuevoEstado = if (detalle?.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
                        viewModel.cambiarEstadoUsuario(idUsuario, nuevoEstado)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (detalle?.estado == "ACTIVO")
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFF4CAF50)
                    )
                ) {
                    Text(if (detalle?.estado == "ACTIVO") "Inhabilitar" else "Habilitar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de Usuario") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { padding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6750A4))
            }
            return@Scaffold
        }

        detalle?.let { p ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                // Tarjeta Principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEADDE8)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        // Nombre de usuario con badge de estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                p.usuario,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF6750A4)
                            )

                            // Badge de estado
                            Surface(
                                color = if (p.estado == "ACTIVO")
                                    Color(0xFF4CAF50)
                                else
                                    MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = p.estado ?: "DESCONOCIDO",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        DetailItem(
                            label = "Personal",
                            value = "${p.personal?.nombre ?: ""} ${p.personal?.apellPaterno ?: ""} ${p.personal?.apellMaterno ?: ""}".trim()
                        )
                        DetailItem(label = "Rol", value = p.rol?.descripcion)
                        DetailItem(label = "Fecha Ingreso", value = p.fechaCreacion ?: "-")
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onEdit(p.idUsuario!!) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }

                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (p.estado == "ACTIVO")
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            imageVector = if (p.estado == "ACTIVO")
                                Icons.Default.Block
                            else
                                Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (p.estado == "ACTIVO") "Inhabilitar" else "Habilitar")
                    }
                }
            }
        }
    }
}
@Composable
fun DetailItem(label: String, value: String?) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: "N/A",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}