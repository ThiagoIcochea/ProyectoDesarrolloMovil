package com.example.gestindeasistencia.utils

/**
 * Configuración de geolocalización para validación de asistencia
 * 
 * IMPORTANTE: Actualizar estas coordenadas con la ubicación real de la empresa
 */
object GeoConfig {
    
    /**
     * Coordenadas de la empresa
     * 
     * Para obtener las coordenadas correctas:
     * 1. Abrir Google Maps
     * 2. Hacer clic derecho en la ubicación de la empresa
     * 3. Seleccionar las coordenadas que aparecen (primer item del menú)
     * 4. Pegar aquí
     * -12.021652585133255, -76.97859200294965
     * Formato: Latitud, Longitud
     */
    const val EMPRESA_LATITUD = -12.129880
    const val EMPRESA_LONGITUD = -76.948594
    
    /**
     * Radio permitido en metros
     * Los empleados solo podrán marcar asistencia si están dentro de este radio
     */
    const val RADIO_PERMITIDO_METROS = 100.0
    
    /**
     * Habilitar/deshabilitar validación de geofencing
     * Útil para pruebas o casos especiales
     */
    const val GEOFENCING_HABILITADO = true
    
    /**
     * Mostrar distancia en la UI
     * Si es true, mostrará cuántos metros está el empleado de la empresa
     */
    const val MOSTRAR_DISTANCIA = true
}

