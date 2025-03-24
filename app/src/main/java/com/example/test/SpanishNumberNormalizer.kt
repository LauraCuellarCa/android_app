// archivo: app/src/main/java/com/example/tuapp/util/SpanishNumberNormalizer.kt
package com.example.tuapp.util

object SpanishNumberNormalizer {
    private val numberMap: Map<String, String> by lazy { createNumberMap() }
    private val decimalSeparators = setOf("punto", "coma", "con")

    private fun createNumberMap(): Map<String, String> {
        val map = mutableMapOf(
            "cero" to "0",
            "un" to "1", "uno" to "1", "una" to "1",
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
            "veinte" to "20"
        )

        // Generación de números 21-29
        listOf("uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve").forEachIndexed { index, suffix ->
            val num = index + 21
            map["veinti$suffix"] = num.toString()
            if (num == 21) {
                map["veintiuna"] = "21"
                map["veintiún"] = "21"
            }
        }

        // Generación de decenas 30-90
        listOf(
            "treinta" to 30, "cuarenta" to 40, "cincuenta" to 50,
            "sesenta" to 60, "setenta" to 70, "ochenta" to 80,
            "noventa" to 90
        ).forEach { (prefix, base) ->
            map[prefix] = base.toString()
            for (i in 1..9) {
                map["$prefix y ${map.entries.firstOrNull { it.value == i.toString() }?.key}"] = (base + i).toString()
            }
        }

        // Números 100-199
        map["cien"] = "100"
        for (i in 1..99) {
            map["ciento ${map.entries.firstOrNull { it.value == i.toString() }?.key}"] = (100 + i).toString()
        }

        // Números 200-900
        listOf(
            "doscientos" to 200, "trescientos" to 300, "cuatrocientos" to 400,
            "quinientos" to 500, "seiscientos" to 600, "setecientos" to 700,
            "ochocientos" to 800, "novecientos" to 900
        ).forEach { (name, value) -> map[name] = value.toString() }

        return map
    }

    fun normalize(text: String): String {
        val words = text.split(" ")
        val result = StringBuilder()
        var i = 0

        while (i < words.size) {
            val currentWord = words[i].lowercase()

            // Detección de números decimales (ej: "tres punto cinco")
            if (i + 2 < words.size && decimalSeparators.contains(words[i+1].lowercase())) {
                val integerPart = numberMap[currentWord] ?: currentWord
                val decimalPart = numberMap[words[i+2].lowercase()] ?: words[i+2]
                result.append("$integerPart.$decimalPart")
                i += 3
            }
            // Detección de números normales (1-999)
            else {
                val possibleNumbers = listOf(
                    currentWord,
                    if (i+1 < words.size) "$currentWord ${words[i+1]}" else null,
                    if (i+2 < words.size) "$currentWord ${words[i+1]} ${words[i+2]}" else null
                ).filterNotNull()

                val numberMatch = possibleNumbers.firstOrNull { numberMap.containsKey(it) }

                if (numberMatch != null) {
                    result.append(numberMap[numberMatch])
                    i += numberMatch.split(" ").size
                } else {
                    result.append(words[i])
                    i++
                }
            }

            if (i < words.size) result.append(" ")
        }

        return result.toString()
    }
}