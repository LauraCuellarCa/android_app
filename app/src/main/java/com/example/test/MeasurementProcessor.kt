package com.example.test

import android.util.Log

class MeasurementProcessor {
    companion object {
        // Patrón para capturar key y value (sin procesar)
// Patrón CORRECTO:
        private val measurementPattern = """(?<keyValue>[a-z]+(?:\s+[a-z]+)?)\s+(?<numberValue>.+)""".toRegex()        // Palabras clave que identifican campos de teléfono
        private val phoneKeywords = listOf("telefono", "movil", "codigo", "cantidad", "nif", "numero identificacion", "stock minimo", "cantidad disponible", "codigo diseno")
        private val fechaKeywords = listOf("fecha nacimiento", "fecha", "fecha entrada", "fecha alta", "fecha creacion")

        fun process(spokenText: String, keyMap: Map<String, Int>, updateField: (Int, String) -> Unit) {
            Log.d("MeasurementProcessor", "Procesando: '$spokenText'")

            // Buscamos coincidencia con el patrón
            val match = measurementPattern.find(spokenText.trim()) ?: run {
                Log.d("MeasurementProcessor", "No se encontró patrón")
                return
            }

            val key = match.groups["keyValue"]?.value?.trim() ?: return
            var rawValue = match.groups["numberValue"]?.value?.trim() ?: return

            rawValue = rawValue
                .replace(" con ", ".", ignoreCase = true)
                .replace(" coma ", ".", ignoreCase = true)

            val parts = rawValue.split(" ")

            if (parts.size == 2) {
                val first = parts[0].toIntOrNull()
                val second = parts[1].toIntOrNull()

                if (first != null && second == 1 && first % 10 == 0) {
                    rawValue = (first + 1).toString()
                }
            }


            // Verificamos si es un campo de teléfono
            val isPhoneField = phoneKeywords.any { keyword ->
                key.contains(keyword, ignoreCase = true)
            }
            val isFechaField = fechaKeywords.any { keyword ->
                key.contains(keyword, ignoreCase = true)
            }
            val isNumeric = rawValue.matches("-?\\d+(\\.\\d+)?".toRegex())

            // Procesamos el valor según el tipo de campo
            val finalValue = if (isPhoneField) {
                // Para teléfono: quitamos TODOS los espacios y caracteres no numéricos
                rawValue.replace("[^0-9]".toRegex(), "")
            }
            else if (isFechaField) {
                // Para fechas: quitamos TODOS los espacios y caracteres no numéricos
                rawValue.replace("[^0-9]".toRegex(), "/")
            }
            else if (isNumeric){
                "$rawValue cm"
            }
            else {
                rawValue
            }

            keyMap[key]?.let { index ->
                Log.d("MeasurementProcessor", "Asignando: $key = $finalValue")
                updateField(index, finalValue)
            } ?: Log.d("MeasurementProcessor", "Key '$key' no encontrada en el mapa")
        }
    }
}