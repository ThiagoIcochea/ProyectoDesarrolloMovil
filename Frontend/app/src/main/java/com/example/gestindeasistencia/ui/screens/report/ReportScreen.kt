package com.example.gestindeasistencia.ui.screens.report

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.app.DatePickerDialog
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.gestindeasistencia.viewmodels.AsistenciaViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ReportRow(
	val nombre: String,
	val documento: String?,
	val totalAsistencias: Int,
	val demoras: Int,
	val faltas: Int,
	val descuentoPercent: Double,
	val ultimaFecha: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: AsistenciaViewModel, onBack: () -> Unit) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }

	val asistencias = viewModel.asistencias.value
	LaunchedEffect(Unit) { viewModel.cargarAsistencias() }

	var filtroNombre by remember { mutableStateOf("") }
	var fechaDesde by remember { mutableStateOf("") }
	var fechaHasta by remember { mutableStateOf("") }
	var fechaError by remember { mutableStateOf<String?>(null) }

	val asistenciasFiltradas = remember(asistencias, filtroNombre, fechaDesde, fechaHasta) {
		filtrarAsistencias(asistencias, filtroNombre, fechaDesde, fechaHasta)
	}
	val summaries = remember(asistenciasFiltradas) { calcularResumen(asistenciasFiltradas) }

	Scaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = { Text("Reportes de Asistencia", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
				navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás") } },
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF6750A4), titleContentColor = Color.White)
			)
		},
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		containerColor = MaterialTheme.colorScheme.surface
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.padding(padding)
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.surface),
			contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(20.dp)
		) {
			item { Text("Gestión de Reportes", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp), color = Color(0xFF6750A4)) }

			item {
				Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDE8))) {
					Column(modifier = Modifier.padding(16.dp)) {
						Text("Filtros de búsqueda", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF6750A4))
						Spacer(modifier = Modifier.height(12.dp))
						OutlinedTextField(value = filtroNombre, onValueChange = { filtroNombre = it }, label = { Text("Buscar por nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
						Spacer(modifier = Modifier.height(12.dp))
						Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
							OutlinedButton(onClick = { mostrarSelectorFecha(context) { selected -> fechaDesde = selected } }, modifier = Modifier.weight(1f)) {
								Icon(Icons.Default.DateRange, contentDescription = null)
								Spacer(modifier = Modifier.width(8.dp))
								Text(if (fechaDesde.isBlank()) "Desde" else fechaDesde, fontSize = 12.sp)
							}
							OutlinedButton(onClick = { mostrarSelectorFecha(context) { selected -> fechaHasta = selected } }, modifier = Modifier.weight(1f)) {
								Icon(Icons.Default.DateRange, contentDescription = null)
								Spacer(modifier = Modifier.width(8.dp))
								Text(if (fechaHasta.isBlank()) "Hasta" else fechaHasta, fontSize = 12.sp)
							}
						}
						Spacer(modifier = Modifier.height(12.dp))
						Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
							Button(onClick = { val valido = validarRangoFechas(fechaDesde, fechaHasta); fechaError = if (!valido.first) valido.second else null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))) { Text("Aplicar", fontWeight = FontWeight.Bold) }
							OutlinedButton(onClick = { filtroNombre = ""; fechaDesde = ""; fechaHasta = ""; fechaError = null }, modifier = Modifier.weight(1f)) { Text("Limpiar") }
						}
						if (fechaError != null) { Spacer(modifier = Modifier.height(8.dp)); Text(fechaError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }
					}
				}
			}

			item {
				if (summaries.isNotEmpty()) {
					val totalAsistenciasGlobal = summaries.sumOf { it.totalAsistencias }
					val totalDemorasGlobal = summaries.sumOf { it.demoras }
					val totalFaltasGlobal = summaries.sumOf { it.faltas }
					val descuentoPromedio = summaries.map { it.descuentoPercent }.average()
					Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
						Column(modifier = Modifier.padding(16.dp)) {
							Text("Resumen Global", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF6750A4))
							Spacer(modifier = Modifier.height(12.dp))
							Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
								StatCard(label = "Asistencias", valor = totalAsistenciasGlobal.toString(), bgColor = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
								StatCard(label = "Demoras", valor = totalDemorasGlobal.toString(), bgColor = Color(0xFFFFF3E0), textColor = Color(0xFFE65100), modifier = Modifier.weight(1f))
								StatCard(label = "Faltas", valor = totalFaltasGlobal.toString(), bgColor = Color(0xFFFFEBEE), textColor = Color(0xFFC62828), modifier = Modifier.weight(1f))
								StatCard(label = "Desc. Prom.", valor = "${"%.1f".format(descuentoPromedio)}%", bgColor = Color(0xFFE0F2F1), textColor = Color(0xFF00695C), modifier = Modifier.weight(1f))
							}
						}
					}
				}
			}

			item {
				Button(onClick = { scope.launch { if (summaries.isEmpty()) snackbarHostState.showSnackbar("No hay datos para exportar") else {
							val filename = "reportes_personal_${System.currentTimeMillis()}.csv"
							val uri = exportToCsv(context, filename, summaries)
							snackbarHostState.showSnackbar(if (uri != null) "Archivo exportado: $filename" else "Error al exportar")
						} } }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))) {
					Icon(Icons.Default.FileDownload, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Exportar a Excel", fontWeight = FontWeight.Bold)
				}
			}

			item { if (summaries.isNotEmpty()) Text("Detalle por Personal (${summaries.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF6750A4)) }

			items(summaries) { s ->
				Card(modifier = Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(12.dp)) {
					Column(modifier = Modifier.padding(14.dp)) {
						Text(s.nombre, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF6750A4))
						if (!s.documento.isNullOrBlank()) Text("Doc: ${s.documento}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
						Spacer(modifier = Modifier.height(10.dp))
						Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
							StatIndicator(label = "Asistencias", valor = s.totalAsistencias.toString(), color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
							StatIndicator(label = "Demoras", valor = s.demoras.toString(), color = Color(0xFFE65100), modifier = Modifier.weight(1f))
							StatIndicator(label = "Faltas", valor = s.faltas.toString(), color = Color(0xFFC62828), modifier = Modifier.weight(1f))
							StatIndicator(label = "Descuento", valor = "${"%.1f".format(s.descuentoPercent)}%", color = Color(0xFF00695C), modifier = Modifier.weight(1f))
						}
						if (!s.ultimaFecha.isNullOrBlank()) { Spacer(modifier = Modifier.height(8.dp)); Text("Última marca: ${s.ultimaFecha}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
					}
				}
			}

			item {
				if (summaries.isEmpty() && (filtroNombre.isNotBlank() || fechaDesde.isNotBlank() || fechaHasta.isNotBlank())) {
					Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
						Column(horizontalAlignment = Alignment.CenterHorizontally) {
							Text("No se encontraron resultados", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
							Text("Intenta cambiar los filtros", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
						}
					}
				}
			}

			item { Spacer(modifier = Modifier.height(8.dp)) }
		}
	}
}

@Composable
fun StatCard(label: String, valor: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier) {
	Card(modifier = modifier.height(80.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = bgColor)) {
		Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
			Text(valor, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor, fontSize = 18.sp)
			Text(label, style = MaterialTheme.typography.labelSmall, color = textColor, fontSize = 10.sp)
		}
	}
}

@Composable
fun StatIndicator(label: String, valor: String, color: Color, modifier: Modifier = Modifier) {
	Column(modifier = modifier.background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp)).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
		Text(valor, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = color, fontSize = 14.sp)
		Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
	}
}

fun calcularResumen(asistencias: List<com.example.gestindeasistencia.data.models.AsistenciaDto>): List<ReportRow> {
	val porPersonal = asistencias.groupBy { it.personal?.idPersonal }
	return porPersonal.mapNotNull { (_, registros) ->
		val personal = registros.firstOrNull()?.personal ?: return@mapNotNull null
		val nombre = listOfNotNull(personal.nombre, personal.apellPaterno, personal.apellMaterno).joinToString(" ").ifBlank { "Sin nombre" }
		val documento = personal.nroDocumento
		val totalAsistencias = registros.count { r -> (r.movimiento?.descripcion ?: "").contains("entrada", true) || (r.movimiento?.abreDesc ?: "").equals("ENT", true) }
		val demoras = registros.count { r -> (r.movimiento?.descripcion ?: "").contains("tarde", true) || (r.movimiento?.descripcion ?: "").contains("demora", true) }
		val faltas = registros.count { r -> (r.movimiento?.descripcion ?: "").contains("falta", true) || (r.movimiento?.abreDesc ?: "").equals("FAL", true) }
		val descuentoPercent = faltas * 5.0
		val ultimaFecha = registros.maxByOrNull { it.fecha ?: "" }?.fecha
		ReportRow(nombre = nombre, documento = documento, totalAsistencias = totalAsistencias, demoras = demoras, faltas = faltas, descuentoPercent = descuentoPercent, ultimaFecha = ultimaFecha)
	}
}

fun filtrarAsistencias(asistencias: List<com.example.gestindeasistencia.data.models.AsistenciaDto>, filtroNombre: String, fechaDesde: String, fechaHasta: String): List<com.example.gestindeasistencia.data.models.AsistenciaDto> {
	fun extraerFechaIso(fechaHora: String?): String? = fechaHora?.takeIf { it.isNotBlank() }?.take(10)
	return asistencias.filter { a ->
		val personal = a.personal
		val nombreCompleto = listOfNotNull(personal?.nombre, personal?.apellPaterno, personal?.apellMaterno).joinToString(" ").lowercase()
		val matchNombre = filtroNombre.isBlank() || nombreCompleto.contains(filtroNombre.lowercase())
		val fecha = extraerFechaIso(a.fecha)
		val matchDesde = fechaDesde.isBlank() || (fecha != null && fecha >= fechaDesde)
		val matchHasta = fechaHasta.isBlank() || (fecha != null && fecha <= fechaHasta)
		matchNombre && matchDesde && matchHasta
	}
}

fun mostrarSelectorFecha(context: Context, onFechaSeleccionada: (String) -> Unit) {
	val cal = Calendar.getInstance()
	DatePickerDialog(context, { _, y, m, d -> cal.set(y, m, d); val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); onFechaSeleccionada(sdf.format(cal.time)) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
}

fun validarRangoFechas(fechaDesde: String, fechaHasta: String): Pair<Boolean, String?> {
	if (fechaDesde.isBlank() && fechaHasta.isBlank()) return Pair(true, null)
	val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
	return try {
		val desde = if (fechaDesde.isBlank()) null else sdf.parse(fechaDesde)
		val hasta = if (fechaHasta.isBlank()) null else sdf.parse(fechaHasta)
		if (fechaDesde.isNotBlank() && desde == null) return Pair(false, "Fecha 'Desde' con formato inválido")
		if (fechaHasta.isNotBlank() && hasta == null) return Pair(false, "Fecha 'Hasta' con formato inválido")
		if (desde != null && hasta != null && desde.after(hasta)) return Pair(false, "La fecha 'Desde' debe ser anterior o igual a 'Hasta'")
		Pair(true, null)
	} catch (e: Exception) { Pair(false, "Formato de fecha inválido (usar yyyy-MM-dd)") }
}

suspend fun exportToCsv(context: Context, filename: String, rows: List<ReportRow>): Uri? {
	val resolver = context.contentResolver
	val mime = "text/csv"
	val displayName = filename
	val contentValues = ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, displayName); put(MediaStore.MediaColumns.MIME_TYPE, mime); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(MediaStore.Downloads.IS_PENDING, 1) }
	val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Files.getContentUri("external")
	val uri = resolver.insert(collection, contentValues) ?: return null
	resolver.openOutputStream(uri)?.use { outputStream -> BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { bw -> bw.write("Nombre,Documento,Asistencias,Demoras,Faltas,DescuentoPercent,UltimaMarca\n"); rows.forEach { r -> bw.write("${r.nombre},${r.documento ?: ""},${r.totalAsistencias},${r.demoras},${r.faltas},${"%.1f".format(r.descuentoPercent)},${r.ultimaFecha ?: ""}\n") }; bw.flush() } }
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { contentValues.clear(); contentValues.put(MediaStore.Downloads.IS_PENDING, 0); resolver.update(uri, contentValues, null, null) }
	return uri
}
