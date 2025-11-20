package com.example.gestindeasistencia.ui.screens.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gestindeasistencia.viewmodels.PersonalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailScreen(
    idPersonal: Int,
    viewModel: PersonalViewModel,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onDeleted: () -> Unit
) {
    val detalle by viewModel.detalle.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()

    LaunchedEffect(idPersonal) {
        viewModel.cargarDetalle(idPersonal)
    }

    if (deleteSuccess) {
        onDeleted()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle del Personal") },
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
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFF6750A4)) }
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
                        containerColor = Color(0xFFEADDE8) // Color de Card del modelo
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            "${p.nombre} ${p.apellPaterno} ${p.apellMaterno}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF6750A4)
                        )

                        Spacer(Modifier.height(16.dp))

                        DetailItem(label = "Cargo", value = p.cargo?.descripcion)
                        DetailItem(label = "Documento", value = "${p.documento?.descripcion} - ${p.nroDocumento}")
                        DetailItem(label = "Fecha Ingreso", value = p.fechaIngreso ?: "—")
                        DetailItem(label = "Fecha Nacimiento", value = p.fechaNacimiento ?: "—")
                    }
                }


                Spacer(Modifier.height(32.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onEdit(p.idPersonal!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }

                    Button(
                        onClick = { viewModel.eliminar(p.idPersonal!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar")
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
