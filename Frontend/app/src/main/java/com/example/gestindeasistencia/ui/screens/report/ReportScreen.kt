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
import com.example.gestindeasistencia.data.models.UsuarioDto
import com.example.gestindeasistencia.data.remote.ApiClient
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
import java.util.Date
import java.util.Locale

data class ReportRow(
	val nombre: String,
	val documento: String?,
	val totalAsistencias: Int,
	val totalMarcas: Int,
	val diasTrabajados: Int,
	val demoras: Int,
	val faltas: Int,
	val descuentoPercent: Double,
	val totalEntries: Int,
	val totalExits: Int,
	val missingEntries: Int,
	val missingExits: Int,
	val diasFaltantes: List<String>,
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

	// cargar usuarios para obtener fechaCreacion por usuario
	var usuarios by remember { mutableStateOf<List<UsuarioDto>>(emptyList()) }
	LaunchedEffect(Unit) {
		try {
			val api = ApiClient.getClient(context)
			val resp = api.listarUsuarios()
			if (resp.isSuccessful && resp.body() != null) usuarios = resp.body()!!
		} catch (_: Exception) { /* ignore */ }
	}

	var filtroNombre by remember { mutableStateOf("") }
	var fechaDesde by remember { mutableStateOf("") }
	var fechaHasta by remember { mutableStateOf("") }
	var fechaError by remember { mutableStateOf<String?>(null) }

	val asistenciasFiltradas = remember(asistencias, filtroNombre, fechaDesde, fechaHasta) {
		var resultado = asistencias
		// primero filtrar por fecha
		if (fechaDesde.isNotBlank() || fechaHasta.isNotBlank()) {
			fun extraerFechaIso(fechaHora: String?): String? = fechaHora?.takeIf { it.isNotBlank() }?.take(10)
			resultado = resultado.filter { a ->
				val fecha = extraerFechaIso(a.fecha)
				val matchDesde = fechaDesde.isBlank() || (fecha != null && fecha >= fechaDesde)
				val matchHasta = fechaHasta.isBlank() || (fecha != null && fecha <= fechaHasta)
				matchDesde && matchHasta
			}
		}
		// luego filtrar por nombre (ocultar quiénes no coincidan)
		if (filtroNombre.isNotBlank()) {
			resultado = resultado.filter { a ->
				val nombreCompleto = listOfNotNull(a.personal?.nombre, a.personal?.apellPaterno, a.personal?.apellMaterno).joinToString(" ").lowercase()
				nombreCompleto.contains(filtroNombre.lowercase())
			}
		}
		resultado
	}

	val periodoInicio = if (fechaDesde.isNotBlank()) fechaDesde else "2025-10-01"
	val periodoFin = if (fechaHasta.isNotBlank()) fechaHasta else java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())

	val summaries = remember(asistenciasFiltradas, periodoInicio, periodoFin, usuarios) {
		calcularResumen(asistenciasFiltradas, periodoInicio, periodoFin, usuarios)
	}

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
					val totalMarcasGlobal = summaries.sumOf { it.totalMarcas }
					val totalDiasGlobal = summaries.sumOf { it.diasTrabajados }
					val totalEntriesGlobal = summaries.sumOf { it.totalEntries }
					val totalExitsGlobal = summaries.sumOf { it.totalExits }
					val totalMissingEntriesGlobal = summaries.sumOf { it.missingEntries }
					val totalMissingExitsGlobal = summaries.sumOf { it.missingExits }
					Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
						Column(modifier = Modifier.padding(16.dp)) {
							Text("Resumen Global", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF6750A4))
							Spacer(modifier = Modifier.height(12.dp))
							Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
								StatCard(label = "Entradas", valor = totalEntriesGlobal.toString(), bgColor = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
								StatCard(label = "Salidas", valor = totalExitsGlobal.toString(), bgColor = Color(0xFFFFF3E0), textColor = Color(0xFFE65100), modifier = Modifier.weight(1f))
								StatCard(label = "Faltó Entrada", valor = totalMissingEntriesGlobal.toString(), bgColor = Color(0xFFFFEBEE), textColor = Color(0xFFC62828), modifier = Modifier.weight(1f))
								StatCard(label = "Faltó Salida", valor = totalMissingExitsGlobal.toString(), bgColor = Color(0xFFE0F2F1), textColor = Color(0xFF00695C), modifier = Modifier.weight(1f))
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
							StatIndicator(label = "Entradas", valor = s.totalEntries.toString(), color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
							StatIndicator(label = "Salidas", valor = s.totalExits.toString(), color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
							StatIndicator(label = "Faltó Entr.", valor = s.missingEntries.toString(), color = Color(0xFFC62828), modifier = Modifier.weight(1f))
							StatIndicator(label = "Faltó Sal.", valor = s.missingExits.toString(), color = Color(0xFFC62828), modifier = Modifier.weight(1f))
						}
					if (s.diasFaltantes.isNotEmpty()) {
						Spacer(modifier = Modifier.height(8.dp))
						Text("Días con faltantes: ${s.diasFaltantes.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

fun calcularResumen(asistencias: List<com.example.gestindeasistencia.data.models.AsistenciaDto>, periodoInicio: String, periodoFin: String, usuarios: List<UsuarioDto>): List<ReportRow> {
	// Nueva implementación más robusta y explícita
	val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

	// parseo flexible de fecha/hora
	fun parseDateFlexible(fechaHora: String?): Date? {
		if (fechaHora.isNullOrBlank()) return null
		val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")
		for (p in patterns) {
			try {
				return SimpleDateFormat(p, Locale.getDefault()).parse(fechaHora)
			} catch (_: Exception) { }
		}
		return null
	}

	// convierte a fecha sin hora
	fun dateOnly(d: Date): Date {
		val c = Calendar.getInstance(); c.time = d
		c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
		return c.time
	}

	// convierte yyyy-MM-dd string a Date start of day
	val inicioGlobal = try { sdfDate.parse(periodoInicio) } catch (_: Exception) { null }
	val finGlobalRaw = try { sdfDate.parse(periodoFin) } catch (_: Exception) { null }

	val today = dateOnly(Date())
	val finGlobal = when {
		finGlobalRaw == null -> today
		finGlobalRaw.after(today) -> today
		else -> finGlobalRaw
	}

	fun workingDaysBetween(start: Date?, end: Date?): List<Date> {
		if (start == null || end == null) return emptyList()
		val res = mutableListOf<Date>()
		val c = Calendar.getInstance(); c.time = dateOnly(start)
		val endOnly = dateOnly(end)
		while (!c.time.after(endOnly)) {
			val dow = c.get(Calendar.DAY_OF_WEEK)
			if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) res.add(c.time)
			c.add(Calendar.DATE, 1)
		}
		return res
	}

	// construir mapa de personal SOLO con quiénes aparezcan en asistencias filtradas
	val personalMap = mutableMapOf<Int, com.example.gestindeasistencia.data.models.PersonalDto>()
	asistencias.forEach { a -> a.personal?.idPersonal?.let { id -> a.personal?.let { p -> personalMap[id] = p } } }

	val limiteTardanzaMinutes = 8 * 60 + 15
	val results = mutableListOf<ReportRow>()

	// procesar cada personal que tenga registros en asistencias filtradas
	for ((idPersonal, personal) in personalMap) {
		val registros = asistencias.filter { it.personal?.idPersonal == idPersonal }

		// fecha de inicio por usuario: fechaCreacion + 1 si existe, sino inicioGlobal
		val usuario = usuarios.firstOrNull { it.personal?.idPersonal == idPersonal }
		val inicioUsuarioDate = usuario?.fechaCreacion?.let { s -> parseDateFlexible(s)?.let { d ->
			val c = Calendar.getInstance(); c.time = d; c.add(Calendar.DATE, 1); dateOnly(c.time)
		} }
		val inicioFinal = when {
			inicioGlobal == null && inicioUsuarioDate == null -> null
			inicioGlobal == null -> inicioUsuarioDate
			inicioUsuarioDate == null -> dateOnly(inicioGlobal)
			else -> if (inicioUsuarioDate.after(inicioGlobal)) inicioUsuarioDate else dateOnly(inicioGlobal)
		}

		// tope por usuario: min(finGlobal, today, ultimaMarca)
		val ultimaMarcaDate = registros.mapNotNull { parseDateFlexible(it.fecha) }.maxByOrNull { it.time }
		val ultimaMarcaOnly = ultimaMarcaDate?.let { dateOnly(it) }
		val finUsuario = listOfNotNull(finGlobal, today, ultimaMarcaOnly).minOrNull()

		// dias laborales para el usuario en el rango
		val diasLaborales = workingDaysBetween(inicioFinal, finUsuario)
		val diasLaboralesSet = diasLaborales.map { sdfDate.format(it) }.toSet()

		// fechas con cualquier marca del usuario
		val fechasConMarca = registros.mapNotNull { it.fecha?.take(10) }.toSet()

		// considerar solo las marcas que caen dentro de diasLaborales
		val diasPresentes = fechasConMarca.intersect(diasLaboralesSet)

		// contar demoras: por cada dia presente, tomar la primera marca de entrada (o la primera en general)
		var demoras = 0
		val registrosPorFecha = registros.groupBy { it.fecha?.take(10) ?: "" }
		for (dia in diasPresentes) {
			val marcasDelDia = registrosPorFecha[dia] ?: continue
			val entradas = marcasDelDia.filter { r ->
				val desc = r.movimiento?.descripcion?.lowercase() ?: ""
				val cod = r.movimiento?.abreDesc ?: ""
				desc.contains("entrada") || desc.contains("ingreso") || cod.equals("ENT", true)
			}
			val marcaEntrada = entradas.minByOrNull { it.fecha ?: "" } ?: marcasDelDia.minByOrNull { it.fecha ?: "" }
			val minutos = marcaEntrada?.fecha?.let { fh -> parseDateFlexible(fh)?.let { dt ->
				val c = Calendar.getInstance(); c.time = dt; c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
			} }
			if (minutos != null && minutos > limiteTardanzaMinutes) demoras += 1
		}

		val totalMarcas = registros.size
		val diasTrabajados = diasPresentes.size
		val faltas = maxOf(0, diasLaboralesSet.size - diasTrabajados)
		val descuento = faltas * 5.0 + demoras * 2.0

		// contar tipos de marcas en el conjunto de registros
		val isEntry: (com.example.gestindeasistencia.data.models.AsistenciaDto) -> Boolean = { r ->
			val desc = r.movimiento?.descripcion?.lowercase() ?: ""
			val cod = r.movimiento?.abreDesc ?: ""
			desc.contains("entrada") || desc.contains("ingreso") || cod.equals("ENT", true)
		}
		val isExit: (com.example.gestindeasistencia.data.models.AsistenciaDto) -> Boolean = { r ->
			val desc = r.movimiento?.descripcion?.lowercase() ?: ""
			val cod = r.movimiento?.abreDesc ?: ""
			desc.contains("salida") || cod.equals("SAL", true)
		}
		val isStartBreak: (com.example.gestindeasistencia.data.models.AsistenciaDto) -> Boolean = { r ->
			val desc = r.movimiento?.descripcion?.lowercase() ?: ""
			val cod = r.movimiento?.abreDesc ?: ""
			desc.contains("break") && (desc.contains("inicio") || cod.equals("EBR", true))
		}
		val isEndBreak: (com.example.gestindeasistencia.data.models.AsistenciaDto) -> Boolean = { r ->
			val desc = r.movimiento?.descripcion?.lowercase() ?: ""
			val cod = r.movimiento?.abreDesc ?: ""
			desc.contains("break") && (desc.contains("fin") || desc.contains("fin") || cod.equals("FBR", true))
		}

		val totalEntries = registros.count { isEntry(it) }
		val totalExits = registros.count { isExit(it) }

		// por día, verificar faltantes (solo entradas y salidas)
		var missingEntries = 0
		var missingExits = 0
		val diasFaltantesList = mutableListOf<String>()
		for (d in diasLaboralesSet) {
			val marcasDelDia = registrosPorFecha[d] ?: emptyList()
			val hasEntry = marcasDelDia.any { isEntry(it) }
			val hasExit = marcasDelDia.any { isExit(it) }
			if (!hasEntry) missingEntries += 1
			if (!hasExit) missingExits += 1
			if (!hasEntry || !hasExit) diasFaltantesList.add(d)
		}

		val nombre = listOfNotNull(personal.nombre, personal.apellPaterno, personal.apellMaterno).joinToString(" ").ifBlank { "Sin nombre" }
		val documento = personal.nroDocumento
		val ultimaFecha = registros.mapNotNull { it.fecha }.maxOrNull()

		results.add(ReportRow(
			nombre = nombre,
			documento = documento,
			totalAsistencias = diasTrabajados,
			totalMarcas = totalMarcas,
			diasTrabajados = diasTrabajados,
			demoras = demoras,
			faltas = faltas,
			descuentoPercent = descuento,
			totalEntries = totalEntries,
			totalExits = totalExits,
			missingEntries = missingEntries,
			missingExits = missingExits,
			diasFaltantes = diasFaltantesList,
			ultimaFecha = ultimaFecha
		))
	}

	return results.sortedBy { it.nombre }
}

fun filtrarAsistencias(asistencias: List<com.example.gestindeasistencia.data.models.AsistenciaDto>): List<com.example.gestindeasistencia.data.models.AsistenciaDto> {
	// Esta función ya no se usa; la lógica está en ReportScreen. Mantenerla para compatibilidad.
	return asistencias
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
	resolver.openOutputStream(uri)?.use { outputStream -> BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { bw -> bw.write("Nombre,Documento,Entradas,Salidas,Faltó_Entrada,Faltó_Salida,Cant_Días_Faltantes,Detalle_Días_Faltantes\n"); rows.forEach { r -> bw.write("${r.nombre},${r.documento ?: ""},${r.totalEntries},${r.totalExits},${r.missingEntries},${r.missingExits},${r.diasFaltantes.size},\"${r.diasFaltantes.joinToString(";")}\"\n") }; bw.flush() } }
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { contentValues.clear(); contentValues.put(MediaStore.Downloads.IS_PENDING, 0); resolver.update(uri, contentValues, null, null) }
	return uri
}
