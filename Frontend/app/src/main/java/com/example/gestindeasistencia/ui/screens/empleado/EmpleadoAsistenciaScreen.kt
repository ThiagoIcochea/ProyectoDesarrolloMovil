package com.example.gestindeasistencia.ui.screens.empleado

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.gestindeasistencia.BuildConfig
import com.example.gestindeasistencia.data.models.AsistenciaCreateRequest
import com.example.gestindeasistencia.utils.BiometricHelper
import com.example.gestindeasistencia.utils.DeviceHelper
import com.example.gestindeasistencia.utils.GeoConfig
import com.example.gestindeasistencia.utils.LocationHelper
import com.example.gestindeasistencia.viewmodels.AsistenciaViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoAsistenciaScreen(
    viewModel: AsistenciaViewModel,
    username: String?,
    onNavigateBack: (() -> Unit)? = null,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var biometricHelper by remember { mutableStateOf<BiometricHelper?>(null) }
    var locationHelper by remember { mutableStateOf<LocationHelper?>(null) }
    
    var currentLatitude by remember { mutableStateOf<Double?>(null) }
    var currentLongitude by remember { mutableStateOf<Double?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var pendingMovimientoId by remember { mutableStateOf<Int?>(null) }
    var pendingMovimientoName by remember { mutableStateOf<String?>(null) }
    var currentTime by remember { mutableStateOf(Date()) }

    // Inicializar helpers de forma segura
    LaunchedEffect(Unit) {
        try {
            biometricHelper = BiometricHelper(context)
            locationHelper = LocationHelper(context)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al inicializar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Calcular si está dentro del rango permitido
    val estaDentroDeRango = remember(currentLatitude, currentLongitude, locationHelper) {
        val lat = currentLatitude
        val lng = currentLongitude
        val helper = locationHelper
        
        if (lat != null && lng != null && helper != null) {
            helper.estaDentroDeEmpresa(lat, lng)
        } else {
            false
        }
    }

    val distanciaEmpresa = remember(currentLatitude, currentLongitude, locationHelper) {
        val lat = currentLatitude
        val lng = currentLongitude
        val helper = locationHelper
        
        if (lat != null && lng != null && helper != null) {
            helper.obtenerDistanciaDesdeEmpresa(lat, lng)
        } else {
            null
        }
    }

    // Determinar qué botones están habilitados según el flujo
    // Control de flujo de asistencia basado en el último movimiento registrado
    val listaAsistencias by viewModel.asistencias
    val ultimoMovimientoId = listaAsistencias.lastOrNull()?.movimiento?.idMovimiento
    
    val entradaHabilitada = ultimoMovimientoId == null || ultimoMovimientoId == 2
    val salidaHabilitada = ultimoMovimientoId == 1 || ultimoMovimientoId == 4
    val entradaBreakHabilitada = ultimoMovimientoId == 1 || ultimoMovimientoId == 4
    val finBreakHabilitado = ultimoMovimientoId == 3

    // Actualizar hora cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            locationHelper?.getLastLocation(
                onSuccess = { lat, lng ->
                    currentLatitude = lat
                    currentLongitude = lng
                    locationError = null
                },
                onError = { error ->
                    locationError = error
                }
            )
        }
    }

    // Cargar datos al iniciar (con manejo de errores)
    LaunchedEffect(Unit) {
        try {
            viewModel.cargarMovimientos()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar movimientos", Toast.LENGTH_SHORT).show()
        }
        
        try {
            viewModel.cargarAsistencias()
        } catch (e: Exception) {
            // Ignorar error si no puede cargar asistencias
        }
        
        // Solicitar permisos de ubicación
        delay(500) // Dar tiempo para que se inicialice todo
        if (locationHelper?.hasLocationPermission() == true) {
            locationHelper?.getLastLocation(
                onSuccess = { lat, lng ->
                    currentLatitude = lat
                    currentLongitude = lng
                },
                onError = { error ->
                    locationError = error
                }
            )
        } else {
            locationPermissionLauncher.launch(LocationHelper.LOCATION_PERMISSIONS)
        }
    }

    // Observar mensajes de éxito
    LaunchedEffect(viewModel.successMessage.value) {
        viewModel.successMessage.value?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajes()
        }
    }

    // Observar mensajes de error
    LaunchedEffect(viewModel.errorMessage.value) {
        viewModel.errorMessage.value?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sistema de Asistencia") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de bienvenida
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEADDE8)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF6750A4)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bienvenido/a",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = username ?: "Empleado",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                            .format(currentTime),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(currentTime),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Estado de ubicación y geofencing
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        currentLatitude == null -> Color(0xFFFFEBEE)        // Sin GPS = Rojo claro
                        estaDentroDeRango -> Color(0xFFE8F5E9)              // Dentro = Verde claro
                        else -> Color(0xFFFFF3E0)                           // Fuera = Naranja claro
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            currentLatitude == null -> Icons.Default.LocationOff
                            estaDentroDeRango -> Icons.Default.CheckCircle
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when {
                            currentLatitude == null -> Color(0xFFD32F2F)    // Rojo
                            estaDentroDeRango -> Color(0xFF4CAF50)          // Verde
                            else -> Color(0xFFFF9800)                       // Naranja
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when {
                                currentLatitude == null -> "Ubicación no disponible"
                                estaDentroDeRango -> "✓ Dentro de la empresa"
                                else -> "⚠ Fuera del rango permitido"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentLatitude != null && currentLongitude != null) {
                            Text(
                                text = "Lat: %.6f, Lng: %.6f".format(currentLatitude, currentLongitude),
                                style = MaterialTheme.typography.bodySmall
                            )
                            distanciaEmpresa?.let { dist ->
                                Text(
                                    text = "Distancia: ${dist.toInt()}m / ${GeoConfig.RADIO_PERMITIDO_METROS.toInt()}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (estaDentroDeRango) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (locationError != null) {
                            Text(
                                text = locationError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Marcar Asistencia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de marcado - 2x2 Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón ENTRADA
                    AsistenciaButton(
                        icon = Icons.Default.Login,
                        text = "Entrada",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f),
                        enabled = !viewModel.isLoading.value && entradaHabilitada,
                        onClick = {
                            if (!entradaHabilitada) {
                                Toast.makeText(context, "⚠️ Primero debes marcar Salida", Toast.LENGTH_SHORT).show()
                                return@AsistenciaButton
                            }
                            
                            if (currentLatitude == null) {
                                Toast.makeText(context, "Esperando ubicación GPS...", Toast.LENGTH_SHORT).show()
                                return@AsistenciaButton
                            }
                            
                            // Validar que esté dentro del rango de la empresa
                            if (!estaDentroDeRango) {
                                val distancia = distanciaEmpresa?.toInt() ?: 0
                                Toast.makeText(
                                    context,
                                    "⚠️ Debes estar en la empresa para marcar asistencia\n" +
                                    "Distancia actual: ${distancia}m (máximo: ${GeoConfig.RADIO_PERMITIDO_METROS.toInt()}m)",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@AsistenciaButton
                            }
                            
                            pendingMovimientoId = 1
                            pendingMovimientoName = "Entrada"
                            showBiometricDialog = true
                        }
                    )

                    // Botón SALIDA
                    AsistenciaButton(
                        icon = Icons.Default.Logout,
                        text = "Salida",
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f),
                        enabled = !viewModel.isLoading.value && salidaHabilitada,
                        onClick = {
                            if (!salidaHabilitada) {
                                Toast.makeText(context, "⚠️ Primero debes marcar Entrada", Toast.LENGTH_SHORT).show()
                                return@AsistenciaButton
                            }
                            
                            if (currentLatitude == null) {
                                Toast.makeText(context, "Esperando ubicación GPS...", Toast.LENGTH_SHORT).show()
                                return@AsistenciaButton
                            }
                            
                            // Validar que esté dentro del rango de la empresa
                            if (!estaDentroDeRango) {
                                val distancia = distanciaEmpresa?.toInt() ?: 0
                                Toast.makeText(
                                    context,
                                    "⚠️ Debes estar en la empresa para marcar asistencia\n" +
                                    "Distancia actual: ${distancia}m (máximo: ${GeoConfig.RADIO_PERMITIDO_METROS.toInt()}m)",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@AsistenciaButton
                            }
                            
                            pendingMovimientoId = 2
                            pendingMovimientoName = "Salida"
                            showBiometricDialog = true
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón ENTRADA BREAK
                    AsistenciaButton(
                        icon = Icons.Default.Coffee,
                        text = "Entrada\nBreak",
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f),
                        enabled = !viewModel.isLoading.value && entradaBreakHabilitada,
                        onClick = {
                            if (!entradaBreakHabilitada) {
                                val mensaje = when (ultimoMovimientoId) {
                                    null, 2 -> "⚠️ Primero debes marcar Entrada"
                                    3 -> "⚠️ Primero debes marcar Fin Break"
                                    else -> "⚠️ No puedes marcar Entrada Break ahora"
                                }
                                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                return@AsistenciaButton
                            }
                            
                            if (currentLatitude == null) {
                                Toast.makeText(context, "Esperando ubicación GPS...", Toast.LENGTH_SHORT).show()
                                return@AsistenciaButton
                            }
                            
                            // Validar que esté dentro del rango de la empresa
                            if (!estaDentroDeRango) {
                                val distancia = distanciaEmpresa?.toInt() ?: 0
                                Toast.makeText(
                                    context,
                                    "⚠️ Debes estar en la empresa para marcar asistencia\n" +
                                    "Distancia actual: ${distancia}m (máximo: ${GeoConfig.RADIO_PERMITIDO_METROS.toInt()}m)",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@AsistenciaButton
                            }
                            
                            pendingMovimientoId = 3
                            pendingMovimientoName = "Entrada Break"
                            showBiometricDialog = true
                        }
                    )

                    // Botón FIN BREAK
                    AsistenciaButton(
                        icon = Icons.Default.RestaurantMenu,
                        text = "Fin\nBreak",
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f),
                        enabled = !viewModel.isLoading.value && finBreakHabilitado,
                        onClick = {
                            if (!finBreakHabilitado) {
                                Toast.makeText(context, "⚠️ Primero debes marcar Entrada Break", Toast.LENGTH_SHORT).show()
                                return@AsistenciaButton
                            }
                            
                            if (currentLatitude == null) {
                                Toast.makeText(context, "Esperando ubicación GPS...", Toast.LENGTH_SHORT).show()
                                return@AsistenciaButton
                            }
                            
                            // Validar que esté dentro del rango de la empresa
                            if (!estaDentroDeRango) {
                                val distancia = distanciaEmpresa?.toInt() ?: 0
                                Toast.makeText(
                                    context,
                                    "⚠️ Debes estar en la empresa para marcar asistencia\n" +
                                    "Distancia actual: ${distancia}m (máximo: ${GeoConfig.RADIO_PERMITIDO_METROS.toInt()}m)",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@AsistenciaButton
                            }
                            
                            pendingMovimientoId = 4
                            pendingMovimientoName = "Fin Break"
                            showBiometricDialog = true
                        }
                    )
                }
            }

            if (viewModel.isLoading.value) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Últimas asistencias
            if (viewModel.asistencias.value.isNotEmpty()) {
                Text(
                    text = "Últimas marcaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                viewModel.asistencias.value.takeLast(5).reversed().forEach { asistencia ->
                    // Obtener descripción del movimiento basado en ID
                    val movimientoDesc = when(asistencia.movimiento?.idMovimiento) {
                        1 -> "Entrada"
                        2 -> "Salida"
                        3 -> "Entrada Break"
                        4 -> "Fin Break"
                        else -> asistencia.movimiento?.descripcion ?: "Marcación"
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = movimientoDesc,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = asistencia.fecha ?: "N/A",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (asistencia.ipMarcador?.contains("Lat:") == true) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Con ubicación",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Proceso de autenticación biométrica
        if (showBiometricDialog && context is FragmentActivity) {
            LaunchedEffect(Unit) {
                try {
                    val isEmulator = DeviceHelper.isEmulator()
                    
                    // Si es emulador en DEBUG, no pedir huella (permitir sin huella)
                    if (isEmulator && BuildConfig.DEBUG) {
                        showBiometricDialog = false
                        Toast.makeText(
                            context,
                            "⚠️ MODO PRUEBA (Emulador)\n\nSin huella - solo para desarrollo.",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        val personalId = viewModel.obtenerPersonalIdDeToken()
                        if (personalId != null && pendingMovimientoId != null && 
                            currentLatitude != null && currentLongitude != null) {
                            
                            val ipConLocation = AsistenciaCreateRequest.formatIpWithLocation(
                                "192.168.1.103",
                                currentLatitude!!,
                                currentLongitude!!
                            )
                            
                            viewModel.marcarAsistencia(
                                personalId = personalId,
                                movimientoId = pendingMovimientoId!!,
                                ipConLocation = ipConLocation,
                                onSuccess = {
                                    viewModel.cargarAsistencias()
                                },
                                onError = { error ->
                                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        pendingMovimientoId = null
                        pendingMovimientoName = null
                    } else if (biometricHelper?.isBiometricAvailable() == true) {
                        // Dispositivo físico o emulador en RELEASE - pedir huella
                        biometricHelper?.authenticate(
                            activity = context,
                            title = "Autenticación requerida",
                            subtitle = "Confirma tu identidad para marcar $pendingMovimientoName",
                            onSuccess = {
                                showBiometricDialog = false
                                // Autenticación exitosa, proceder con el marcado
                                val personalId = viewModel.obtenerPersonalIdDeToken()
                                if (personalId != null && pendingMovimientoId != null && 
                                    currentLatitude != null && currentLongitude != null) {
                                    
                                    val ipConLocation = AsistenciaCreateRequest.formatIpWithLocation(
                                        "192.168.1.103",
                                        currentLatitude!!,
                                        currentLongitude!!
                                    )
                                    
                                    viewModel.marcarAsistencia(
                                        personalId = personalId,
                                        movimientoId = pendingMovimientoId!!,
                                        ipConLocation = ipConLocation,
                                        onSuccess = {
                                            // Forzar recarga de asistencias
                                            viewModel.cargarAsistencias()
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                pendingMovimientoId = null
                                pendingMovimientoName = null
                            },
                            onError = { error ->
                                showBiometricDialog = false
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                pendingMovimientoId = null
                                pendingMovimientoName = null
                            }
                        )
                    } else {
                        // Huella NO disponible
                        showBiometricDialog = false
                        
                        val isEmulator = DeviceHelper.isEmulator()
                        val shouldRequire = DeviceHelper.shouldRequireBiometric()
                        
                        // Si es emulador en DEBUG, permitir sin huella para pruebas
                        if (isEmulator && BuildConfig.DEBUG) {
                            Toast.makeText(
                                context,
                                "⚠️ MODO PRUEBA (Emulador)\n\nSin huella - solo para desarrollo.",
                                Toast.LENGTH_LONG
                            ).show()
                            
                            val personalId = viewModel.obtenerPersonalIdDeToken()
                            if (personalId != null && pendingMovimientoId != null && 
                                currentLatitude != null && currentLongitude != null) {
                                
                                val ipConLocation = AsistenciaCreateRequest.formatIpWithLocation(
                                    "192.168.1.103",
                                    currentLatitude!!,
                                    currentLongitude!!
                                )
                                
                                viewModel.marcarAsistencia(
                                    personalId = personalId,
                                    movimientoId = pendingMovimientoId!!,
                                    ipConLocation = ipConLocation,
                                    onSuccess = {
                                        viewModel.cargarAsistencias()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        } else if (shouldRequire) {
                            // Dispositivo físico sin huella o emulador en RELEASE - BLOQUEAR
                            Toast.makeText(
                                context,
                                "⚠️ HUELLA OBLIGATORIA\n\nEste dispositivo no tiene sensor de huella digital. No puedes marcar asistencia.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        pendingMovimientoId = null
                        pendingMovimientoName = null
                    }
                } catch (e: Exception) {
                    showBiometricDialog = false
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    pendingMovimientoId = null
                    pendingMovimientoName = null
                }
            }
        } else if (showBiometricDialog) {
            // Si no es FragmentActivity
            LaunchedEffect(Unit) {
                showBiometricDialog = false
                
                val isEmulator = DeviceHelper.isEmulator()
                val shouldRequire = DeviceHelper.shouldRequireBiometric()
                
                // Si es emulador en DEBUG, permitir sin huella para pruebas
                if (isEmulator && BuildConfig.DEBUG) {
                    Toast.makeText(
                        context,
                        "⚠️ MODO PRUEBA (Emulador)\n\nSin verificación biométrica - solo desarrollo.",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    val personalId = viewModel.obtenerPersonalIdDeToken()
                    if (personalId != null && pendingMovimientoId != null && 
                        currentLatitude != null && currentLongitude != null) {
                        
                        val ipConLocation = AsistenciaCreateRequest.formatIpWithLocation(
                            "192.168.1.103",
                            currentLatitude!!,
                            currentLongitude!!
                        )
                        
                        viewModel.marcarAsistencia(
                            personalId = personalId,
                            movimientoId = pendingMovimientoId!!,
                            ipConLocation = ipConLocation,
                            onSuccess = {
                                viewModel.cargarAsistencias()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } else if (shouldRequire) {
                    // Dispositivo físico sin huella o emulador en RELEASE - BLOQUEAR
                    Toast.makeText(
                        context, 
                        "⚠️ HUELLA OBLIGATORIA\n\nNo se puede verificar la huella. Contacta al administrador.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                pendingMovimientoId = null
                pendingMovimientoName = null
            }
        }
    }
}

@Composable
fun AsistenciaButton(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
