package com.example.gestindeasistencia.ui.screens.usuario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gestindeasistencia.data.models.PersonalDto
import com.example.gestindeasistencia.data.models.RolDto
import com.example.gestindeasistencia.data.models.UsuarioDto
import com.example.gestindeasistencia.viewmodels.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioFormScreen(
    idUsuario: Int?,
    viewModel: UsuarioViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val detalle by viewModel.detalle.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // Lista de personal disponible
    val listaPersonal by viewModel.listaPersonal.collectAsState()

    // Estados del formulario
    var selectedPersonal by remember { mutableStateOf<PersonalDto?>(null) }
    var selectedRolId by remember { mutableStateOf<Int?>(null) }
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("ACTIVO") }
    var fechaCreacion by remember { mutableStateOf("") }

    // Estados para dropdowns
    var expandedPersonal by remember { mutableStateOf(false) }
    var expandedRol by remember { mutableStateOf(false) }
    var expandedEstado by remember { mutableStateOf(false) }

    // Errores de validación
    var errorUsuario by remember { mutableStateOf(false) }
    var errorPersonal by remember { mutableStateOf(false) }
    var errorRol by remember { mutableStateOf(false) }

    // Roles disponibles (1: Administrador, 2: Trabajador)
    val rolesDisponibles = listOf(
        1 to "Administrador",
        2 to "Trabajador"
    )

    LaunchedEffect(idUsuario) {
        if (idUsuario != null) {
            viewModel.cargaUsuario(idUsuario)
        }
        // Cargar lista de personal
        viewModel.cargarListaPersonal()
    }

    LaunchedEffect(detalle) {
        detalle?.let { p ->
            selectedRolId = p.rol?.idRol
            selectedPersonal = p.personal
            usuario = p.usuario
            password = p.password ?: ""
            estado = p.estado ?: "ACTIVO"
            fechaCreacion = p.fechaCreacion ?: ""
        }
    }

    if (saveSuccess) {
        LaunchedEffect(Unit) {
            viewModel.resetSaveSuccess()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (idUsuario == null) "Nuevo Usuario" else "Editar Usuario") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de Personal
            ExposedDropdownMenuBox(
                expanded = expandedPersonal,
                onExpandedChange = { expandedPersonal = it }
            ) {
                OutlinedTextField(
                    value = selectedPersonal?.let {
                        "${it.nombre} ${it.apellPaterno} ${it.apellMaterno}"
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Personal *") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Seleccionar personal"
                        )
                    },
                    isError = errorPersonal,
                    supportingText = if (errorPersonal) {
                        { Text("Debe seleccionar un personal") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded = expandedPersonal,
                    onDismissRequest = { expandedPersonal = false }
                ) {
                    listaPersonal.forEach { personal ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "${personal.nombre} ${personal.apellPaterno} ${personal.apellMaterno}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Doc: ${personal.nroDocumento} - ${personal.cargo?.descripcion ?: "Sin cargo"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                selectedPersonal = personal
                                errorPersonal = false
                                expandedPersonal = false
                            }
                        )
                    }
                }
            }

            // Selector de Rol (Simplificado: 1=Administrador, 2=Trabajador)
            ExposedDropdownMenuBox(
                expanded = expandedRol,
                onExpandedChange = { expandedRol = it }
            ) {
                OutlinedTextField(
                    value = when(selectedRolId) {
                        1 -> "Administrador"
                        2 -> "Trabajador"
                        else -> ""
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rol *") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Seleccionar rol"
                        )
                    },
                    isError = errorRol,
                    supportingText = if (errorRol) {
                        { Text("Debe seleccionar un rol") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedRol,
                    onDismissRequest = { expandedRol = false }
                ) {
                    rolesDisponibles.forEach { (id, descripcion) ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(descripcion)
                                    Text(
                                        text = if (id == 1) "👨‍💼" else "👷",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            },
                            onClick = {
                                selectedRolId = id
                                errorRol = false
                                expandedRol = false
                            }
                        )
                    }
                }
            }

            // Campo Usuario
            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    errorUsuario = it.isBlank()
                },
                label = { Text("Usuario *") },
                isError = errorUsuario,
                supportingText = if (errorUsuario) {
                    { Text("El usuario es requerido") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (idUsuario == null) "Contraseña *" else "Nueva Contraseña (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(if (idUsuario == null) "Requerido para nuevo usuario" else "Dejar vacío para mantener la actual")
                }
            )

            // Selector de Estado
            ExposedDropdownMenuBox(
                expanded = expandedEstado,
                onExpandedChange = { expandedEstado = it }
            ) {
                OutlinedTextField(
                    value = estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Seleccionar estado"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedEstado,
                    onDismissRequest = { expandedEstado = false }
                ) {
                    listOf("ACTIVO", "INACTIVO").forEach { estadoItem ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(estadoItem)
                                    Text(
                                        text = if (estadoItem == "ACTIVO") "✓" else "✗",
                                        color = if (estadoItem == "ACTIVO")
                                            Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                }
                            },
                            onClick = {
                                estado = estadoItem
                                expandedEstado = false
                            }
                        )
                    }
                }
            }

            // Campo Fecha Creación (solo lectura si está editando)
            OutlinedTextField(
                value = fechaCreacion,
                onValueChange = { fechaCreacion = it },
                label = { Text("Fecha Creación (YYYY-MM-DD)") },
                enabled = idUsuario == null,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(if (idUsuario != null) "Fecha de creación original" else "Formato: YYYY-MM-DD")
                }
            )

            Spacer(Modifier.height(8.dp))

            // Botón Guardar
            Button(
                onClick = {
                    // Validaciones
                    errorUsuario = usuario.isBlank()
                    errorPersonal = selectedPersonal == null
                    errorRol = selectedRolId == null

                    if (!errorUsuario && !errorPersonal && !errorRol && selectedRolId != null) {
                        val dto = UsuarioDto(
                            idUsuario = idUsuario,
                            rol = RolDto(
                                idRol = selectedRolId,
                                descripcion = when(selectedRolId) {
                                    1 -> "Administrador"
                                    2 -> "Trabajador"
                                    else -> null
                                },
                                estado = "ACTIVO"
                            ),
                            personal = selectedPersonal,
                            usuario = usuario,
                            password = password.ifBlank { null },
                            estado = estado,
                            fechaCreacion = fechaCreacion.ifBlank { null }
                        )

                        if (idUsuario == null) {
                            viewModel.crearUsuario(dto)
                        } else {
                            viewModel.actualizarUsuario(idUsuario, dto)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Text(if (idUsuario == null) "Guardar Usuario" else "Actualizar Usuario")
            }
        }
    }
}