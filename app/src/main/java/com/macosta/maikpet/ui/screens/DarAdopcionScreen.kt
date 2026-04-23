package com.macosta.maikpet.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.macosta.maikpet.ui.components.InfoCard
import com.macosta.maikpet.ui.theme.*
import com.macosta.maikpet.util.ImageUtils
import com.macosta.maikpet.util.SoundPlayer

@Composable
fun DarAdopcionScreen(
    isLoggedIn: Boolean,
    currentUserName: String?,
    isLoading: Boolean,
    isGeocoding: Boolean = false,
    geocodingMessage: String? = null,
    onSubmit: (nombre: String, tipo: String, edadMeses: Int, vacunas: String, direccion: String, descripcion: String, imagenBase64: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Perro") }
    var edadMeses by remember { mutableStateOf("") }
    var vacunas by remember { mutableStateOf("Si") }
    var direccion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    var lastSubmittedTipo by remember { mutableStateOf("") }
    var triggerSound by remember { mutableStateOf(false) }
    var addressSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var showVentaError by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedImageBase64 = ImageUtils.convertUriToBase64(context, it)
        }
    }

    LaunchedEffect(triggerSound) {
        if (triggerSound && lastSubmittedTipo.isNotEmpty()) {
            when (lastSubmittedTipo) {
                "Perro" -> SoundPlayer.playDogBark(context)
                "Gato" -> SoundPlayer.playCatMeow(context)
            }
            lastSubmittedTipo = ""
            triggerSound = false
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedImageUri = null
            selectedImageBase64 = ImageUtils.bitmapToBase64(it)
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 80.dp)
            .verticalScroll(scrollState)
    ) {
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Dar en adopcion",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Completa el formulario para publicar una mascota",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Border)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isLoggedIn) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Debes iniciar sesion para publicar mascotas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✅", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logueado como ${currentUserName ?: "Usuario"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showImageOptions = true }
                ) {
                    if (selectedImageUri != null || selectedImageBase64 != null) {
                        Box {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Foto seleccionada",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = {
                                    selectedImageUri = null
                                    selectedImageBase64 = null
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .background(DeleteRed, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar",
                                    tint = OnPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                                .border(3.dp, Primary.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Agregar foto",
                                tint = Primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca para agregar una foto",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            if (showImageOptions) {
                AlertDialog(
                    onDismissRequest = { showImageOptions = false },
                    title = { Text("Agregar foto") },
                    text = { Text("¿Cómo quieres agregar la foto?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showImageOptions = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Icon(Icons.Default.CameraAlt, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Cámara")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showImageOptions = false
                            imagePickerLauncher.launch("image/*")
                        }) {
                            Icon(Icons.Default.Photo, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Galería")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("🐕 Nombre del animal") },
                placeholder = { Text("Ej: Luna, Max, Simba") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "🐾 Tipo de animal",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tipo == "Perro",
                    onClick = { tipo = "Perro" },
                    label = { Text("🐶 Perro") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = OnPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = tipo == "Gato",
                    onClick = { tipo = "Gato" },
                    label = { Text("🐱 Gato") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = OnPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = edadMeses,
                onValueChange = { if (it.all { char -> char.isDigit() }) edadMeses = it },
                label = { Text("📅 Edad (en meses)") },
                placeholder = { Text("Ej: 6") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "💉 Vacunas",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = vacunas == "Si",
                    onClick = { vacunas = "Si" },
                    label = { Text("✅ Si") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = OnPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = vacunas == "No",
                    onClick = { vacunas = "No" },
                    label = { Text("❌ No") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = OnPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = direccion,
                onValueChange = { newValue ->
                    direccion = newValue
                    showSuggestions = newValue.length >= 3
                    if (newValue.length >= 3) {
                        scope.launch {
                            addressSuggestions = withContext(Dispatchers.IO) { getAddressSuggestions(newValue) }
                        }
                    } else {
                        addressSuggestions = emptyList()
                    }
                },
                label = { Text("📍 Direccion donde esta el animal") },
                placeholder = { Text("Ej: Rivera 3142, Montevideo") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    if (direccion.isNotEmpty()) {
                        IconButton(onClick = { 
                            direccion = ""
                            addressSuggestions = emptyList()
                        }) {
                            Icon(Icons.Default.Close, "Limpiar", tint = TextSecondary)
                        }
                    }
                }
            )
            
            if (showSuggestions && addressSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column {
                        addressSuggestions.take(5).forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        direccion = suggestion
                                        showSuggestions = false
                                        addressSuggestions = emptyList()
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnBackground
                                )
                            }
                            if (suggestion != addressSuggestions.take(5).last()) {
                                HorizontalDivider(color = Border, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("📝 Descripcion adicional") },
                placeholder = { Text("Comportamiento, necesidades especiales...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = textFieldColors(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )
            
            if (showVentaError) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeleteRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚠️", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No se permiten ventas, cruzas ni permutas. Solo adopciones gratuitas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeleteRed
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isGeocoding && geocodingMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "📍 Buscando ubicación",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = geocodingMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = {
                    val edad = edadMeses.toIntOrNull() ?: 0
                    val descLower = descripcion.lowercase()
                    val palabrasProhibidas = listOf(
                        "venta", "vendo", "precio", "dolares", "ufs", "u\$s", 
                        "comprar", "compro", "costo", "valor", "\$", "permuta", 
                        "cambio", "canje", "cruza", "cruzar", "aparear", "apareamiento",
                        "monta", "montar", "reproducir", "cria", "crias", "criar",
                        "pedigri", "acoplar", "cubrir"
                    )
                    val tieneProhibida = palabrasProhibidas.any { descLower.contains(it) }
                    
                    if (tieneProhibida) {
                        showVentaError = true
                        return@Button
                    }
                    showVentaError = false
                    
                    if (nombre.isNotBlank() && edad > 0 && direccion.isNotBlank()) {
                        lastSubmittedTipo = tipo
                        triggerSound = true
                        onSubmit(nombre, tipo, edad, vacunas, direccion, descripcion, selectedImageBase64)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && !isGeocoding && nombre.isNotBlank() && edadMeses.isNotBlank() && direccion.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(30.dp)
            ) {
                if (isLoading || isGeocoding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = OnPrimary
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Publicar para adopcion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "La dirección se geolocalizará y aparecerá en el mapa",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private suspend fun getAddressSuggestions(query: String): List<String> {
    return try {
        val encodedQuery = java.net.URLEncoder.encode(query + ", Uruguay", "UTF-8")
        val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=5&countrycodes=uy"
        
        val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        connection.setRequestProperty("User-Agent", "MaikPet/1.0")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        val response = connection.inputStream.bufferedReader().readText()
        val jsonArray = org.json.JSONArray(response)
        
        val suggestions = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val displayName = obj.getString("display_name")
            val parts = displayName.split(",").map { it.trim() }
            val shortAddress = if (parts.size >= 3) {
                "${parts[0]}, ${parts[1]}, ${parts[2]}"
            } else {
                displayName.take(60)
            }
            suggestions.add(shortAddress)
        }
        suggestions
    } catch (e: Exception) {
        emptyList()
    }
}
