package com.example.test
import android.util.Log


class MeasurementProcessor {
    companion object {
        // Mapa para convertir números en palabras a dígitos
        private val spanishNumbers = mapOf(
            "cero" to "0",
            "uno" to "1", "una" to "1", "un" to "1",
            "dos" to "2",
            "tres" to "3",
            "cuatro" to "4",
            "cinco" to "5",
            "seis" to "6",
            "siete" to "7",
            "ocho" to "8",
            "nueve" to "9",
            "diez" to "10",
            "once" to "11",
            "doce" to "12",
            "trece" to "13",
            "catorce" to "14",
            "quince" to "15",
            "dieciséis" to "16", "dieciseis" to "16",
            "diecisiete" to "17",
            "dieciocho" to "18",
            "diecinueve" to "19",
            "veinte" to "20",
            "veintiuno" to "21", "veintiuna" to "21",
            "veintidós" to "22", "veintidos" to "22",
            "veintitrés" to "23", "veintitres" to "23",
            "veinticuatro" to "24",
            "veinticinco" to "25",
            "veintiséis" to "26", "veintiseis" to "26",
            "veintisiete" to "27",
            "veintiocho" to "28",
            "veintinueve" to "29",
            "treinta" to "30",
            "cuarenta" to "40",
            "cincuenta" to "50",
            "sesenta" to "60",
            "setenta" to "70",
            "ochenta" to "80",
            "noventa" to "90",
            "cien" to "100", "ciento" to "100"
        )

        // Lista de unidades de medida para evitar capturarlas como números
        private val units = listOf(
            "cm", "centímetros", "centímetro", "c.m.", "cms", 
            "m", "metros", "metro", "milímetros", "milímetro", "mm"
        )
        
        // Construir un patrón para unidades como alternativas (cm|centímetros|...)
        private val unitsPattern = units.joinToString("|")

        // Expresión regular modificada para separar correctamente número y unidades
        private val measurementPattern =
            """(?<keyValue>\b\w+(?:\s+\w+)*\b)(?:\s+(?:es|es igual a|mide|de|en|lee|muestra|aproximadamente|alrededor de|aproximadamente\s+es|era))?\s+(?<numberValue>\d+(?:\.\d+)?|\b(?:${spanishNumbers.keys.joinToString("|")})\b)(?:\s+(?:$unitsPattern))?"""

        // Debug: Mostrar el patrón construido
        init {
            Log.d("MeasurementProcessor", "Patrón regex: $measurementPattern")
        }

        // Método para convertir números escritos a dígitos
        private fun convertWordToNumber(word: String): String? {
            return spanishNumbers[word.lowercase().trim()]
        }

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
                // Alternativa: método simple para extraer datos
                trySimpleMatching(lowerCaseText, keyMap, updateField)
                return
            }

            for (match in matches) {
                val keyValue = match.groups["keyValue"]?.value?.trim() ?: continue
                var numberValue = match.groups["numberValue"]?.value?.trim() ?: continue
                
                // Intentar convertir si es un número escrito en palabras
                if (!numberValue.matches(Regex("\\d+(\\.\\d+)?"))) {
                    val convertedNumber = convertWordToNumber(numberValue)
                    if (convertedNumber != null) {
                        Log.d("MeasurementProcessor", "Convertido número en palabras: '$numberValue' a '$convertedNumber'")
                        numberValue = convertedNumber
                    } else {
                        Log.d("MeasurementProcessor", "No se pudo convertir: '$numberValue' a un número")
                        continue // Si no podemos convertir, saltamos esta coincidencia
                    }
                }
                
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
        
        // Método alternativo de matching para casos donde el regex falla
        private fun trySimpleMatching(text: String, keyMap: Map<String, Int>, updateField: (Int, String) -> Unit) {
            Log.d("MeasurementProcessor", "Intentando método simple de emparejamiento")
            
            // Buscar cada clave por separado
            for ((key, index) in keyMap) {
                if (text.contains(key)) {
                    // Si encontramos la clave, buscar número después de la clave
                    val keyPosition = text.indexOf(key) + key.length
                    val textAfterKey = text.substring(keyPosition)
                    
                    // Verificar todos los números en español después de la clave
                    for ((numWord, numValue) in spanishNumbers) {
                        if (textAfterKey.contains(numWord)) {
                            Log.d("MeasurementProcessor", "Método simple - Encontrado: '$key' con valor '$numWord' ($numValue)")
                            updateField(index, "$numValue cm")
                            break
                        }
                    }
                    
                    // Buscar también dígitos
                    val digitMatch = Regex("\\d+").find(textAfterKey)
                    if (digitMatch != null) {
                        val numValue = digitMatch.value
                        Log.d("MeasurementProcessor", "Método simple - Encontrado: '$key' con valor numérico '$numValue'")
                        updateField(index, "$numValue cm")
                    }
                }
            }
        }
    }
}
