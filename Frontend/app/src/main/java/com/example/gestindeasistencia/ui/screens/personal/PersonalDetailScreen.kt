package com.example.gestindeasistencia.ui.screens.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        detalle?.let { p ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
            ) {

                Text("${p.nombre} ${p.apellPaterno} ${p.apellMaterno}",
                    style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(10.dp))

                Text("Cargo: ${p.cargo}")
                Text("Documento: ${p.documento} - ${p.nroDocumento}")
                Text("Email: ${p.email}")
                Text("Fecha ingreso: ${p.fechaIngreso ?: "—"}")

                Spacer(Modifier.height(20.dp))

                Row {
                    Button(
                        onClick = { onEdit(p.idPersonal!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }

                    Spacer(Modifier.width(16.dp))

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
