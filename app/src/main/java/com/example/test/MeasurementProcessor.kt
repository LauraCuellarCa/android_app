package com.example.test

import android.util.Log

class MeasurementProcessor {
    companion object {
        // Expresión regex modificada para separar correctamente número y unidades
        private val measurementPattern =
            """(?<keyValue>.+?)\s*(?<numberValue>\d+(?:\.\d+)?).*"""


        fun process(spokenText: String, keyMap: Map<String, Int>, updateField: (Int, String) -> Unit) {

            val matches = Regex(measurementPattern).findAll(spokenText)

            if (matches.none()) {
                trySimpleMatching(spokenText, keyMap, updateField)
                return
            }

            matches.forEach { match ->
                val key = match.groups["keyValue"]?.value?.trim() ?: return@forEach
                val number = match.groups["numberValue"]?.value?.trim() ?: return@forEach

                keyMap[key]?.let { index ->
                    updateField(index, "$number cm")
                    Log.d("MeasurementProcessor", "Asignado: $key = $number cm")
                }
            }
        }

        private fun trySimpleMatching(text: String, keyMap: Map<String, Int>, updateField: (Int, String) -> Unit) {
            keyMap.forEach { (key, index) ->
                if (text.contains(key)) {
                    val remainingText = text.substringAfter(key)
                    Regex("\\d+").find(remainingText)?.let {
                        updateField(index, "${it.value} cm")
                    }
                }
            }
        }
    }
}