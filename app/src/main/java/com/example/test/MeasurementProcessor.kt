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

            Log.d("SpokenText: ", lowerCaseText)

            for (match in matches) {
                val keyValue = match.groups["keyValue"]?.value?.trim() ?: continue
                val numberValue = match.groups["numberValue"]?.value ?: continue
                val measurement = "$numberValue cm"

                // Si el key_value está en el mapa, asignamos el valor al campo correspondiente
                keyMap[keyValue]?.let { fieldIndex ->
                    updateField(fieldIndex, measurement)
                }
            }
        }
    }
}
