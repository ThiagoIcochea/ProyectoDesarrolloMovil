package com.example.gestindeasistencia.ui.screens.personal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestindeasistencia.viewmodels.PersonalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalListScreen(
    viewModel: PersonalViewModel,
    onSelect: (Int) -> Unit,
    onNew: () -> Unit
) {
    val lista by viewModel.lista.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(true) {
        viewModel.cargarPersonal()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNew) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Personal")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            if (loading) {
                CircularProgressIndicator()
                return@Column
            }

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
                return@Column
            }

            LazyColumn {
                items(lista.size) { i ->
                    val p = lista[i]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onSelect(p.idPersonal!!) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${p.nombre} ${p.apellPaterno} ${p.apellMaterno}")
                            Text("Cargo: ${p.cargo}")
                            Text("Documento: ${p.documento} ${p.nroDocumento}")
                        }
                    }
                }
            }
        }
    }
}
