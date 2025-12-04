package com.example.gestindeasistencia.ui.screens.usuario

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.gestindeasistencia.data.models.UsuarioDto
import com.example.gestindeasistencia.viewmodels.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioListScreen(
    viewModel: UsuarioViewModel,
    onSelect: (Int) -> Unit,
    onNew: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val lista by viewModel.lista.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var usuarioToChange by remember { mutableStateOf<UsuarioDto?>(null) }

    LaunchedEffect(true) {
        viewModel.listarUsuarios()
    }

    if (saveSuccess) {
        LaunchedEffect(Unit) {
            viewModel.resetSaveSuccess()
            viewModel.listarUsuarios()
        }
    }

    // Diálogo de confirmación
    if (showDialog && usuarioToChange != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    imageVector = if (usuarioToChange?.estado == "ACTIVO")
                        Icons.Default.Block
                    else
                        Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (usuarioToChange?.estado == "ACTIVO")
                        MaterialTheme.colorScheme.error
                    else
                        Color(0xFF4CAF50)
                )
            },
            title = {
                Text(
                    if (usuarioToChange?.estado == "ACTIVO")
                        "¿Inhabilitar Usuario?"
                    else
                        "¿Habilitar Usuario?"
                )
            },
            text = {
                Text(
                    if (usuarioToChange?.estado == "ACTIVO")
                        "¿Está seguro que desea inhabilitar al usuario '${usuarioToChange?.usuario}'? No podrá iniciar sesión."
                    else
                        "¿Está seguro que desea habilitar al usuario '${usuarioToChange?.usuario}'? Podrá iniciar sesión nuevamente."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        usuarioToChange?.let { usuario ->
                            val nuevoEstado = if (usuario.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
                            viewModel.cambiarEstadoUsuario(usuario.idUsuario!!, nuevoEstado)
                        }
                        showDialog = false
                        usuarioToChange = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (usuarioToChange?.estado == "ACTIVO")
                            MaterialTheme.colorScheme.error
                        else
                            Color(0xFF4CAF50)
                    )
                ) {
                    Text(if (usuarioToChange?.estado == "ACTIVO") "Inhabilitar" else "Habilitar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    usuarioToChange = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Usuarios") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNew,
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Usuario")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF6750A4)
                )
            }

            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Error: $error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lista, key = { it.idUsuario ?: it.usuario.hashCode() }) { p ->
                    val isActive = p.estado == "ACTIVO"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { p.idUsuario?.let { onSelect(it) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive)
                                MaterialTheme.colorScheme.surface
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Usuario con icono
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "Usuario: ${p.usuario}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF6750A4)
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                // Rol con icono
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (p.rol?.idRol == 1)
                                            Icons.Default.AdminPanelSettings
                                        else
                                            Icons.Default.Work,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Rol: ${p.rol?.descripcion ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Badge de estado
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    color = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = p.estado ?: "DESCONOCIDO",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Botón de cambiar estado
                            IconButton(
                                onClick = {
                                    usuarioToChange = p
                                    showDialog = true
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = if (isActive)
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                    else
                                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                    contentDescription = if (isActive) "Inhabilitar" else "Habilitar",
                                    tint = if (isActive) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}