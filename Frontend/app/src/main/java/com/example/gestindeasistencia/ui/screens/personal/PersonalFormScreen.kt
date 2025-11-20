package com.example.gestindeasistencia.ui.screens.personal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestindeasistencia.data.models.CargoDto
import com.example.gestindeasistencia.data.models.DocumentoDto
import com.example.gestindeasistencia.data.models.PersonalDto
import com.example.gestindeasistencia.viewmodels.PersonalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalFormScreen(
    idPersonal: Int?,                      // NULL = crear
    viewModel: PersonalViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val detalle by viewModel.detalle.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // Si es edición, cargar datos
    LaunchedEffect(idPersonal) {
        if (idPersonal != null) viewModel.cargarDetalle(idPersonal)
    }

    if (saveSuccess) {
        onSaved()
    }

    // --- CAMPOS ---
    var nombre by remember { mutableStateOf("") }
    var apellPaterno by remember { mutableStateOf("") }
    var apellMaterno by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nroDocumento by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var fechaIngreso by remember { mutableStateOf("") }

    LaunchedEffect(detalle) {
        detalle?.let { p ->
            nombre = p.nombre ?: ""
            apellPaterno = p.apellPaterno ?: ""
            apellMaterno = p.apellMaterno ?: ""
            email = p.email ?: ""
            nroDocumento = p.nroDocumento ?: ""
            fechaNacimiento = p.fechaNacimiento ?: ""
            fechaIngreso = p.fechaIngreso ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (idPersonal == null) "Nuevo Personal" else "Editar Personal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apellPaterno,
                onValueChange = { apellPaterno = it },
                label = { Text("Apellido Paterno") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = apellMaterno,
                onValueChange = { apellMaterno = it },
                label = { Text("Apellido Materno") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nroDocumento,
                onValueChange = { nroDocumento = it },
                label = { Text("Número de Documento") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha Nacimiento (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fechaIngreso,
                onValueChange = { fechaIngreso = it },
                label = { Text("Fecha Ingreso (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val dto = PersonalDto(
                        idPersonal = idPersonal,
                        cargo = detalle?.cargo ?: CargoDto(1, "Empleado de Planta","ACTIVO"), // OJO
                        documento = detalle?.documento ?: DocumentoDto(1, "DNI"),
                        nombre = nombre,
                        apellPaterno = apellPaterno,
                        apellMaterno = apellMaterno,
                        nroDocumento = nroDocumento,
                        fechaNacimiento = fechaNacimiento,
                        fechaIngreso = fechaIngreso,
                        email = email
                    )

                    if (idPersonal == null)
                        viewModel.crear(dto)
                    else
                        viewModel.actualizar(idPersonal, dto)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (idPersonal == null) "Guardar" else "Actualizar")
            }
        }
    }
}
