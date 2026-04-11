package com.example.pet.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.pet.data.model.Mascota
import com.example.pet.ui.components.InfoCard
import com.example.pet.ui.theme.*
import java.io.ByteArrayOutputStream

@Composable
fun EditarMascotaScreen(
    mascota: Mascota,
    isLoading: Boolean,
    isGeocoding: Boolean = false,
    geocodingMessage: String? = null,
    onSave: (nombre: String, tipo: String, edadMeses: Int, vacunas: String, direccion: String, descripcion: String, imagenBase64: String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf(mascota.nombre) }
    var tipo by remember { mutableStateOf(mascota.tipo) }
    var edadMeses by remember { mutableStateOf(mascota.edadMeses.toString()) }
    var vacunas by remember { mutableStateOf(mascota.vacunas) }
    var direccion by remember { mutableStateOf(mascota.direccion) }
    var descripcion by remember { mutableStateOf(mascota.descripcion ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    var showImageOptions by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedImageBase64 = convertUriToBase64(context, it)
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedImageUri = null
            selectedImageBase64 = bitmapToBase64(it)
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
            .verticalScroll(scrollState)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = OnBackground
                )
            }
            Text(
                text = "Editar ${mascota.nombre}",
                style = MaterialTheme.typography.titleLarge,
                color = OnBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Editando mascota",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Modifica los datos que quieras cambiar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                            model = selectedImageUri ?: selectedImageBase64,
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
                } else if (mascota.imagen != null) {
                    Box {
                        if (mascota.imagen.startsWith("data:image")) {
                            var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                            LaunchedEffect(mascota.imagen) {
                                bitmap = try {
                                    val base64Data = mascota.imagen.substringAfter("base64,")
                                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                } catch (e: Exception) { null }
                            }
                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Foto actual",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, Primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            AsyncImage(
                                model = "https://lmcosturas.com/pet/${mascota.imagen}",
                                contentDescription = "Foto actual",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Primary, CircleShape),
                                contentScale = ContentScale.Crop
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
                    text = "Toca para cambiar la foto",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        
        if (showImageOptions) {
            AlertDialog(
                onDismissRequest = { showImageOptions = false },
                title = { Text("Cambiar foto") },
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
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = OnBackground,
                unfocusedTextColor = OnBackground
            ),
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
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = OnBackground,
                unfocusedTextColor = OnBackground
            ),
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
            onValueChange = { direccion = it },
            label = { Text("📍 Dirección") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = OnBackground,
                unfocusedTextColor = OnBackground
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("📝 Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Border,
                focusedLabelColor = Primary,
                unfocusedLabelColor = TextSecondary,
                focusedTextColor = OnBackground,
                unfocusedTextColor = OnBackground
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )
        
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
                    Text(text = geocodingMessage, color = Primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(
            onClick = {
                val edad = edadMeses.toIntOrNull() ?: 0
                if (nombre.isNotBlank() && edad > 0) {
                    onSave(nombre, tipo, edad, vacunas, direccion, descripcion, selectedImageBase64)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && !isGeocoding && nombre.isNotBlank() && edadMeses.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(30.dp)
        ) {
            if (isLoading || isGeocoding) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = OnPrimary
                )
            } else {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar cambios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeleteRed),
            shape = RoundedCornerShape(30.dp)
        ) {
            Icon(Icons.Default.Delete, null)
            Spacer(Modifier.width(8.dp))
            Text("Eliminar mascota", style = MaterialTheme.typography.titleMedium)
        }
        
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("¿Eliminar ${mascota.nombre}?") },
                text = { Text("Esta acción no se puede deshacer. La mascota será eliminada permanentemente.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                    }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                    }) {
                        Text("Eliminar", color = DeleteRed)
                    }
                }
            )
        }
    }
}

private fun convertUriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmapToBase64(bitmap)
    } catch (e: Exception) {
        null
    }
}

private fun bitmapToBase64(bitmap: Bitmap): String? {
    return try {
        val resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val bytes = outputStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}
