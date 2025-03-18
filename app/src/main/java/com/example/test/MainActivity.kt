package com.example.test

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.test.ui.theme.TestTheme
import java.util.Locale
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Delete

/**
 * MainActivity: The main entry point for the application.
 * This app allows users to input garment measurements using voice recognition.
 * Users can speak measurements like "sleeve width is 20 cm" and the app will 
 * automatically populate the appropriate field.
 */
class MainActivity : ComponentActivity() {
    // State variables for the three measurement input fields
    private var field1 by mutableStateOf("")
    private var field2 by mutableStateOf("")
    private var field3 by mutableStateOf("")

    /**
     * Data class to represent a measurement field's configuration
     * This makes it easier to add new fields or modify existing ones
     */
    data class FieldConfig(
        val id: Int,
        val displayName: String,
        val keywords: List<String>,
        val alternativeTerms: List<String> = emptyList()
    )

    // Lista de configuraciones de campos
    // NOTA: Para añadir nuevos campos o modificar los existentes, edita esta lista
    private val fieldConfigs = listOf(
        FieldConfig(
            id = 0,
            displayName = "Ancho de manga",
            keywords = listOf("ancho", "manga"),  // Palabras clave para identificar este campo
            alternativeTerms = listOf("ancho de manga", "manga ancho", "anchura de manga", "ancho de la manga")  // Frases alternativas completas
        ),
        FieldConfig(
            id = 1,
            displayName = "Largo de manga",
            keywords = listOf("largo", "manga"),  // Palabras clave para identificar este campo
            alternativeTerms = listOf("largo de manga", "manga largo", "longitud de manga", "largo de la manga")  // Frases alternativas completas
        ),
        FieldConfig(
            id = 2,
            displayName = "Ancho de espalda",
            keywords = listOf("ancho", "espalda", "hombros"),  // Palabras clave para identificar este campo (incluye "hombros" como sinónimo)
            alternativeTerms = listOf("ancho de espalda", "espalda ancho", "anchura de espalda", "ancho de la espalda", "ancho de hombros")  // Frases alternativas completas
        )
        // Para añadir un nuevo campo, crea una nueva entrada FieldConfig aquí
        // Por ejemplo:
        // FieldConfig(
        //     id = 3,
        //     displayName = "Largo de espalda",
        //     keywords = listOf("largo", "espalda"),
        //     alternativeTerms = listOf("largo de espalda", "espalda largo", "longitud de espalda")
        // )
    )

    // Mapas derivados automáticamente de la configuración de campos (no es necesario modificarlos)
    private val fieldIdentifiers by lazy {
        fieldConfigs.associate { config -> config.displayName to config.id }
    }

    private val fieldKeywords by lazy {
        fieldConfigs.associate { config -> config.id to config.keywords }
    }

    private val fieldAliases by lazy {
        fieldConfigs.flatMap { config ->
            config.alternativeTerms.map { term -> term to term }
        }.toMap()
    }

    private val patternToFieldIndex by lazy {
        fieldConfigs.flatMap { config ->
            config.alternativeTerms.map { term -> term to config.id }
        }.toMap()
    }

    // Palabras de enlace entre el nombre del campo y el valor
    // NOTA: Puedes añadir más palabras de enlace si es necesario
    private val linkingWords = listOf("es", "mide", "igual a", "de", "tiene", "marca", "aproximadamente", "como")
    
    // Palabras a filtrar por no ser relevantes para la coincidencia de palabras clave
    // NOTA: Modifica esta lista si hay otras palabras que deberían ignorarse
    private val nonRelevantWords = listOf("de", "la", "el", "del", "los", "las", "un", "una")
    
    // Patrón de captura de valor numérico - admite números con punto decimal opcional
    private val capturePattern = "\\d+(?:\\.\\d+)?"
    
    // Indicadores de unidades que pueden seguir al valor
    // NOTA: Puedes añadir más unidades si es necesario
    private val unitPatterns = listOf("cm", "centímetros", "centímetro", "c\\.m\\.", "cms")

    /**
     * Helper method to get the display name of a field by its ID
     */
    private fun getFieldDisplayName(fieldId: Int): String {
        return fieldConfigs.find { it.id == fieldId }?.displayName ?: "Field $fieldId"
    }

    /**
     * Genera un patrón regex a partir de una lista de palabras clave
     * Sigue el algoritmo sugerido para la creación de patrones más generalizables
     * 
     * @param keywords Lista de palabras clave que identifican este campo
     * @return Un patrón regex que coincidirá con texto que contenga estas palabras clave
     */
    private fun generatePatternFromKeywords(keywords: List<String>): String {
        // Filtrar palabras relevantes y añadir límites de palabra
        // NOTA: Esto elimina palabras como "de", "la", etc. y añade \b a cada palabra clave
        val relevantKeywords = keywords.filter { word -> !nonRelevantWords.contains(word) }
        val boundedKeywords = relevantKeywords.map { word -> "\\b$word\\b" }
        
        // Unir palabras clave con patrón que permite palabras arbitrarias entre ellas
        // NOTA: Esto permite que las palabras clave aparezcan en cualquier orden y con otras palabras entre ellas
        val keywordPattern = boundedKeywords.joinToString("(?:\\s+\\w+)*\\s+")
        
        // Crear patrón completo con palabras de enlace opcionales, captura de valor y unidades
        // NOTA: La estructura del patrón es: (keywords) + opcional(palabras de enlace) + valor + unidades
        val linkingPattern = "(?:\\s+(?:" + linkingWords.joinToString("|") + "))?"
        val unitPattern = "\\s*(?:" + unitPatterns.joinToString("|") + ")"
        
        val finalPattern = "($keywordPattern)$linkingPattern\\s+($capturePattern)$unitPattern"
        android.util.Log.d("PatternGeneration", "Generated pattern from keywords $keywords: $finalPattern")
        return finalPattern
    }

    /**
     * Genera todos los patrones regex que se utilizarán para hacer coincidir la entrada de voz
     * @return Lista de objetos Regex compilados a partir de cadenas de patrón
     */
    private fun generateAllPatterns(): List<Regex> {
        val patterns = fieldKeywords.map { (fieldIndex, keywords) ->
            val pattern = generatePatternFromKeywords(keywords)
            android.util.Log.d("RegexGeneration", "Field $fieldIndex (${fieldIdentifiers.entries.find { it.value == fieldIndex }?.key}): $pattern")
            Regex(pattern, RegexOption.IGNORE_CASE)
        }
        android.util.Log.d("RegexGeneration", "Generated ${patterns.size} patterns")
        return patterns
    }

    // Generar los patrones una vez para mejorar el rendimiento
    // NOTA: Los patrones se generan automáticamente a partir de la configuración de campos
    private val allPatterns: List<Regex> by lazy { generateAllPatterns() }

    // Activity result launcher for speech recognition
    // This handles the result from the speech recognition intent
    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""

            processSpokenText(spokenText)
        }
    }

    // Permission request launcher for microphone access
    // This handles the result of the permission request dialog
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(this, "Se requiere permiso del micrófono para el reconocimiento de voz", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initiates the speech recognition process
     * Configures and launches the system speech recognition intent
     */
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Hable ahora...")
            
            // Set a longer speech timeout for better user experience
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000)
            
            // Enable partial results to provide feedback during speech
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            
            // Set max results to improve recognition accuracy
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "El reconocimiento de voz no está soportado en este dispositivo", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Checks for microphone permission and starts speech recognition if granted
     * Requests permission if not already granted
     */
    private fun checkPermissionAndStartSpeechRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PermissionChecker.PERMISSION_GRANTED) {
            startSpeechRecognition()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * Procesa el texto recibido del reconocimiento de voz
     * Extrae valores de medida y actualiza los campos apropiados
     * 
     * @param spokenText El texto recibido del reconocimiento de voz
     */
    private fun processSpokenText(spokenText: String) {
        // Mostrar el texto reconocido completo para depuración
        Toast.makeText(this, "Reconocido: $spokenText", Toast.LENGTH_SHORT).show()
        
        // Registro de depuración para toda la entrada de voz
        android.util.Log.d("SpeechRecognition", "Input: $spokenText")
        
        // Convertir a minúsculas para coincidencia sin distinción entre mayúsculas y minúsculas
        val lowerCaseText = spokenText.lowercase()
        
        // Convertir palabras numéricas a dígitos
        val processedText = convertSpanishNumberWordsToDigits(lowerCaseText)
        android.util.Log.d("SpeechRecognition", "Processed text with converted numbers: $processedText")
        
        // Bandera para rastrear si encontramos alguna coincidencia
        var matchFound = false
        val updatedFields = mutableListOf<String>()
        
        // Probar cada patrón contra el texto hablado
        // NOTA: Este es el núcleo del sistema de coincidencia de patrones
        android.util.Log.d("SpeechRecognition", "Trying to match with ${allPatterns.size} patterns")
        allPatterns.forEachIndexed { index, pattern ->
            val matches = pattern.findAll(processedText).toList()
            android.util.Log.d("SpeechRecognition", "Pattern $index found ${matches.size} matches")
            
            for (match in matches) {
                android.util.Log.d("SpeechRecognition", "Match found: ${match.value}")
                matchFound = true
                
                // Obtener el índice de campo directamente del índice de la lista de patrones
                val fieldIndex = index
                
                // Obtener el valor de medida del segundo grupo de captura (valor numérico)
                val measurementValue = match.groupValues[2]
                android.util.Log.d("SpeechRecognition", "Matched field $fieldIndex with value: $measurementValue")
                
                // Formatear la medida
                val measurement = "$measurementValue cm"
                
                // Actualizar el campo apropiado basado en el índice de campo
                updateFieldWithMeasurement(fieldIndex, measurement, updatedFields)
            }
        }
        
        // Comprobaciones adicionales con el mapa pattern-to-field index para compatibilidad hacia atrás
        // NOTA: Este es un sistema de respaldo si el enfoque principal falla
        if (!matchFound) {
            android.util.Log.d("SpeechRecognition", "No matches found with new patterns, trying fallback method")
            // Comprobar contra patrones tradicionales como respaldo
            for ((pattern, fieldIndex) in patternToFieldIndex) {
                if (processedText.contains(pattern)) {
                    // Extraer medida potencial usando regex
                    val numericPattern = Regex("\\d+(?:\\.\\d+)?\\s*(?:cm|centímetros|centímetro|c\\.m\\.|cms)", RegexOption.IGNORE_CASE)
                    val numericMatch = numericPattern.find(processedText)
                    
                    if (numericMatch != null) {
                        matchFound = true
                        val measurement = numericMatch.value
                        android.util.Log.d("SpeechRecognition", "Fallback match found for field $fieldIndex: $measurement")
                        updateFieldWithMeasurement(fieldIndex, measurement, updatedFields)
                    }
                }
            }
        }
        
        // Mostrar comentarios al usuario sobre qué campos se actualizaron
        if (matchFound) {
            val updatedFieldsStr = updatedFields.joinToString(", ")
            Toast.makeText(this, "Campos actualizados: $updatedFieldsStr", Toast.LENGTH_SHORT).show()
        } else {
            // Si no se encontraron coincidencias, mostrar un mensaje útil con un ejemplo
            Toast.makeText(this, "No se detectaron medidas. Intente decir 'largo de manga es 57 cm'", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Convierte palabras numéricas en español a sus valores numéricos
     * Utilizando expresiones regulares en lugar de un mapa estático
     * 
     * @param text Texto con posibles palabras numéricas
     * @return El mismo texto con palabras numéricas convertidas a dígitos
     */
    private fun convertSpanishNumberWordsToDigits(text: String): String {
        var processedText = text
        
        // Patrones para números simples (0-29)
        val simpleNumberPatterns = mapOf(
            Regex("\\bcero\\b") to "0",
            Regex("\\bun[oa]?\\b") to "1",
            Regex("\\bdos\\b") to "2",
            Regex("\\btres\\b") to "3",
            Regex("\\bcuatro\\b") to "4",
            Regex("\\bcinco\\b") to "5",
            Regex("\\bseis\\b") to "6",
            Regex("\\bsiete\\b") to "7",
            Regex("\\bocho\\b") to "8",
            Regex("\\bnueve\\b") to "9",
            Regex("\\bdiez\\b") to "10",
            Regex("\\bonce\\b") to "11",
            Regex("\\bdoce\\b") to "12",
            Regex("\\btrece\\b") to "13",
            Regex("\\bcatorce\\b") to "14",
            Regex("\\bquince\\b") to "15",
            Regex("\\bdiecis[eé]is\\b") to "16",
            Regex("\\bdiecisiete\\b") to "17",
            Regex("\\bdieciocho\\b") to "18",
            Regex("\\bdiecinueve\\b") to "19",
            Regex("\\bveinte\\b") to "20",
            Regex("\\bveintiun[oa]?\\b") to "21",
            Regex("\\bveintid[oó]s\\b") to "22",
            Regex("\\bveintitr[eé]s\\b") to "23",
            Regex("\\bveinticuatro\\b") to "24",
            Regex("\\bveinticinco\\b") to "25",
            Regex("\\bveintis[eé]is\\b") to "26",
            Regex("\\bveintisiete\\b") to "27",
            Regex("\\bveintiocho\\b") to "28",
            Regex("\\bveintinueve\\b") to "29"
        )
        
        // Patrones para decenas (30-90)
        val tensPatterns = mapOf(
            Regex("\\btreinta\\b") to "30",
            Regex("\\bcuarenta\\b") to "40",
            Regex("\\bcincuenta\\b") to "50",
            Regex("\\bsesenta\\b") to "60",
            Regex("\\bsetenta\\b") to "70",
            Regex("\\bochenta\\b") to "80",
            Regex("\\bnoventa\\b") to "90"
        )
        
        // Patrón para centenas
        val hundredsPattern = Regex("\\bcien(to)?\\b")
        
        // Patrón para construcciones como "treinta y dos"
        val compoundPattern = Regex("\\b(treinta|cuarenta|cincuenta|sesenta|setenta|ochenta|noventa)\\s+y\\s+(un[oa]?|dos|tres|cuatro|cinco|seis|siete|ocho|nueve)\\b")
        
        // Reemplazar patrones compuestos primero
        processedText = processedText.replace(compoundPattern) { match ->
            val tensWord = match.groupValues[1]
            val unitsWord = match.groupValues[2]
            
            val tensValue = when(tensWord) {
                "treinta" -> 30
                "cuarenta" -> 40
                "cincuenta" -> 50
                "sesenta" -> 60
                "setenta" -> 70
                "ochenta" -> 80
                "noventa" -> 90
                else -> 0
            }
            
            val unitsValue = when {
                unitsWord.startsWith("un") -> 1
                unitsWord == "dos" -> 2
                unitsWord == "tres" -> 3
                unitsWord == "cuatro" -> 4
                unitsWord == "cinco" -> 5
                unitsWord == "seis" -> 6
                unitsWord == "siete" -> 7
                unitsWord == "ocho" -> 8
                unitsWord == "nueve" -> 9
                else -> 0
            }
            
            (tensValue + unitsValue).toString()
        }
        
        // Reemplazar números simples
        simpleNumberPatterns.forEach { (regex, replacement) ->
            processedText = processedText.replace(regex, replacement)
        }
        
        // Reemplazar decenas
        tensPatterns.forEach { (regex, replacement) ->
            processedText = processedText.replace(regex, replacement)
        }
        
        // Reemplazar centenas
        processedText = processedText.replace(hundredsPattern, "100")
        
        return processedText
    }

    /**
     * Método auxiliar para actualizar un campo con un valor de medida
     * Extrae la lógica para actualizar campos para evitar duplicación
     * 
     * NOTA: Si añades nuevos campos, necesitarás añadir nuevos casos aquí
     */
    private fun updateFieldWithMeasurement(fieldIndex: Int, measurement: String, updatedFields: MutableList<String>) {
        val displayName = getFieldDisplayName(fieldIndex)
        
        when (fieldIndex) {
            0 -> {
                field1 = measurement
                updatedFields.add(displayName)
                android.util.Log.d("SpeechRecognition", "Updated field1 ($displayName) to: $measurement")
            }
            1 -> {
                field2 = measurement
                updatedFields.add(displayName)
                android.util.Log.d("SpeechRecognition", "Updated field2 ($displayName) to: $measurement")
            }
            2 -> {
                field3 = measurement
                updatedFields.add(displayName)
                android.util.Log.d("SpeechRecognition", "Updated field3 ($displayName) to: $measurement")
            }
            // Para añadir nuevos campos, agregar nuevos casos aquí
            // Por ejemplo:
            // 3 -> {
            //     field4 = measurement  //hay que definir field4 arriba
            //     updatedFields.add(displayName)
            //     android.util.Log.d("SpeechRecognition", "Updated field4 ($displayName) to: $measurement")
            // }
        }
    }

    /**
     * Función auxiliar para encontrar el índice de campo a partir del texto coincidente
     * Utiliza el nuevo enfoque basado en palabras clave primero, con respaldo al método antiguo
     * 
     * @param matchedText El texto para encontrar un campo coincidente
     * @return El índice del campo coincidente, o -1 si no se encuentra coincidencia
     */
    private fun findFieldIndex(matchedText: String): Int {
        // Probar el enfoque de palabras clave primero
        // NOTA: Busca que TODAS las palabras clave de un campo estén en el texto
        fieldKeywords.forEach { (index, keywords) ->
            val allKeywordsPresent = keywords.all { keyword ->
                matchedText.contains(keyword)
            }
            
            if (allKeywordsPresent) {
                return index
            }
        }
        
        // Probar coincidencia exacta como respaldo
        patternToFieldIndex[matchedText]?.let { return it }
        
        // Si no hay coincidencia exacta, probar coincidencias parciales
        for ((pattern, index) in patternToFieldIndex) {
            if (matchedText.contains(pattern)) {
                return index
            }
        }
        
        // Comprobar palabras clave específicas como último recurso
        when {
            matchedText.contains("manga") && (matchedText.contains("ancho") || matchedText.contains("anchura")) -> return 0
            matchedText.contains("manga") && (matchedText.contains("largo") || matchedText.contains("longitud")) -> return 1
            matchedText.contains("espalda") || matchedText.contains("hombros") -> return 2
            //añadir más casos aquí para nuevos campos
        }
        
        return -1  // No se encontró coincidencia
    }

    /**
     * Función auxiliar para capitalizar la primera letra de cada palabra
     * 
     * @return Una cadena con la primera letra de cada palabra en mayúscula
     */
    private fun String.capitalize(): String {
        return this.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) word[0].uppercase() + word.substring(1) else ""
        }
    }

    /**
     * Limpia todos los campos de entrada de medidas
     * NOTA: Si añades más campos, actualiza este método para limpiarlos también
     */
    private fun clearAllFields() {
        field1 = ""
        field2 = ""
        field3 = ""
        // Si añades más campos (field4, etc.), límpialos aquí también
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(
                        modifier = Modifier.padding(innerPadding),
                        field1 = field1,
                        field2 = field2,
                        field3 = field3,
                        onField1Change = { field1 = it },
                        onField2Change = { field2 = it },
                        onField3Change = { field3 = it },
                        onMicClick = { checkPermissionAndStartSpeechRecognition()} ,
                        onClearClick = { clearAllFields() }
                    )
                }
            }
        }
    }
}

/**
 * Main composable that contains the entire UI structure
 * Includes title, input fields, and action buttons
 */
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    field1: String,
    field2: String,
    field3: String,
    onField1Change: (String) -> Unit,
    onField2Change: (String) -> Unit,
    onField3Change: (String) -> Unit,
    onMicClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormTitle()
        InputFields(
            field1 = field1,
            field2 = field2,
            field3 = field3,
            onField1Change = onField1Change,
            onField2Change = onField2Change,
            onField3Change = onField3Change
        )
        // Row for microphone and clear buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botón de micrófono
            IconButton(onClick = onMicClick) {
                Icon(Icons.Default.Mic, contentDescription = "Reconocimiento de voz")
            }
            // Botón de papelera
            IconButton(onClick = onClearClick) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar todos los campos")
            }
        }
    }
}

/**
 * Composable que contiene los tres campos de entrada de medidas
 * NOTA: Si añades nuevos campos, actualiza este composable
 */
@Composable
fun InputFields(
    modifier: Modifier = Modifier,
    field1: String,
    field2: String,
    field3: String,
    // Si añades más campos, agrega sus parámetros aquí
    onField1Change: (String) -> Unit,
    onField2Change: (String) -> Unit,
    onField3Change: (String) -> Unit,
    // Si añades más campos, agrega sus manejadores de cambio aquí
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Campo de ancho de manga
        OutlinedTextField(
            value = field1,
            onValueChange = onField1Change,
            label = { Text("Ancho de manga") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de largo de manga
        OutlinedTextField(
            value = field2,
            onValueChange = onField2Change,
            label = { Text("Largo de manga") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campo de ancho de espalda
        OutlinedTextField(
            value = field3,
            onValueChange = onField3Change,
            label = { Text("Ancho de espalda") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Para añadir un nuevo campo, agrega otro OutlinedTextField aquí:
        /*
        OutlinedTextField(
            value = field4,
            onValueChange = onField4Change,
            label = { Text("Nombre del nuevo campo") },
            modifier = Modifier.fillMaxWidth()
        )
        */
    }
}

/**
 * Title component that displays the form's purpose
 */
@Composable
fun FormTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Formulario de Medidas",
        modifier = modifier
    )
}



