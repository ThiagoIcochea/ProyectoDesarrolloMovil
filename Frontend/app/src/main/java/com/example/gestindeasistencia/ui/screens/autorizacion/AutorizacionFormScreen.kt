package com.example.gestindeasistencia.ui.screens.autorizacion

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestindeasistencia.viewmodels.AutorizacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutorizacionFormScreen(
    viewModel: AutorizacionViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val movimientos by viewModel.movimientos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val operacionExitosa by viewModel.operacionExitosa.collectAsState()

    var selectedMovimientoId by remember { mutableStateOf<Int?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Si se guarda correctamente, navegar atrás
    LaunchedEffect(operacionExitosa) {
        if (operacionExitosa) {
            viewModel.resetOperacion()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nueva Solicitud") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Tipo de Permiso", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown de Movimientos
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it }
            ) {
                val selectedText = movimientos.find { it.idMovimiento == selectedMovimientoId }?.descripcion ?: "Seleccione tipo..."
                OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    movimientos.forEach { mov ->
                        DropdownMenuItem(
                            text = { Text(mov.descripcion ?: "") },
                            onClick = {
                                selectedMovimientoId = mov.idMovimiento
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Justificación / Detalle", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                placeholder = { Text("Explique el motivo de la solicitud...") }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (selectedMovimientoId != null) {
                        viewModel.crearSolicitud(selectedMovimientoId!!, descripcion)
                    }
                },
                enabled = !loading && selectedMovimientoId != null && descripcion.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White)
                else Text("Enviar Solicitud")
            }
        }
    }
}