package com.example.test
import android.util.Log


class MeasurementProcessor {
    companion object {
        // Expresión regular con Named Groups para extraer key_value y number_value
        private val measurementPattern =
            """(?<keyValue>\b\w+(?:\s+\w+)*\b)(?:\s+(?:es|es igual a|mide|de|en|lee|muestra|aproximadamente|alrededor de|aproximadamente\s+es|era))?\s+(?<numberValue>\d+(?:\.\d+)?)\s*(?:cm|centímetros|centímetro|c\.m\.|cms|m|metros|milímetros|mm)?"""

        // Metodo que procesa el texto hablado y asigna los valores a los campos
        fun process(spokenText: String, keyMap: Map<String, Int>, updateField: (Int, String) -> Unit) {
            val lowerCaseText = spokenText.lowercase()
            val pattern = Regex(measurementPattern, RegexOption.IGNORE_CASE)
            val matches = pattern.findAll(lowerCaseText).toList()

            Log.d("MeasurementProcessor", "Texto reconocido: $lowerCaseText")
            Log.d("MeasurementProcessor", "Claves disponibles: ${keyMap.keys.joinToString()}")
            Log.d("MeasurementProcessor", "Número de coincidencias encontradas: ${matches.size}")

            if (matches.isEmpty()) {
                Log.d("MeasurementProcessor", "No se encontraron coincidencias con el patrón")
                return
            }

            for (match in matches) {
                val keyValue = match.groups["keyValue"]?.value?.trim() ?: continue
                val numberValue = match.groups["numberValue"]?.value ?: continue
                val measurement = "$numberValue cm"

                Log.d("MeasurementProcessor", "Coincidencia - Clave: '$keyValue', Valor: '$numberValue'")
                
                // Si el key_value está en el mapa, asignamos el valor al campo correspondiente
                val fieldIndex = keyMap[keyValue]
                if (fieldIndex != null) {
                    Log.d("MeasurementProcessor", "Asignando '$measurement' al campo $fieldIndex ($keyValue)")
                    updateField(fieldIndex, measurement)
                } else {
                    Log.d("MeasurementProcessor", "Clave '$keyValue' no encontrada en el mapa")
                }
            }
        }
    }
}
