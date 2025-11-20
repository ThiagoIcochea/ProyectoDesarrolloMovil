package com.example.gestindeasistencia.ui.screens.personal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestindeasistencia.viewmodels.PersonalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalListScreen(
    viewModel: PersonalViewModel,
    onSelect: (Int) -> Unit,
    onNew: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val lista by viewModel.lista.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(true) {
        viewModel.cargarPersonal()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Personal") },
                // 1. APLICAR COLOR PRIMARIO DEL MODELO (0xFF6750A4)
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
                // 2. APLICAR COLOR PRIMARIO DEL MODELO AL FAB
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Personal")
            }
        },
        // 3. APLICAR FONDO CONTENEDOR SUTIL
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            if (loading) {
                // Indicador de carga sutil
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                return@Column
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lista, key = { it.idPersonal ?: it.nroDocumento.hashCode() }) { p ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { p.idPersonal?.let { onSelect(it) } },
                        // 4. APLICAR ESQUINAS REDONDEADAS Y ELEVACIÓN
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface // Fondo blanco/claro
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            // 5. MEJORAR JERARQUÍA Y COLOR DEL NOMBRE
                            Text(
                                "${p.nombre} ${p.apellPaterno} ${p.apellMaterno}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6750A4)
                            )
                            Spacer(Modifier.height(4.dp))
                            // 6. USAR TIPOGRAFÍA PARA DATOS SECUNDARIOS
                            Text(
                                "Cargo: ${p.cargo?.descripcion ?: "N/A"}", // Usar descripción del cargo
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Documento: ${p.documento?.descripcion ?: "N/A"} ${p.nroDocumento}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}