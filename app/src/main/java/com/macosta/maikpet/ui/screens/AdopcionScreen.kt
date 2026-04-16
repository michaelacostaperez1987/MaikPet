package com.macosta.maikpet.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.macosta.maikpet.data.model.Mascota
import com.macosta.maikpet.ui.components.AdBanner
import com.macosta.maikpet.ui.components.InfoCard
import com.macosta.maikpet.ui.components.LoadingIndicator
import com.macosta.maikpet.ui.components.MascotaCard
import com.macosta.maikpet.ui.theme.*

@Composable
fun AdopcionScreen(
    mascotas: List<Mascota>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            InfoCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Animales en adopcion",
                            style = MaterialTheme.typography.titleLarge,
                            color = OnBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ellos buscan un hogar",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Border)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (isLoading) {
            item {
                LoadingIndicator("Cargando animales...")
            }
        } else if (mascotas.isEmpty()) {
            item {
                InfoCard {
                    Text(
                        text = "No hay mascotas registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(mascotas, key = { it.id }) { mascota ->
                MascotaWhatsAppCard(
                    mascota = mascota,
                    onWhatsAppClick = { telefono ->
                        val numeroLimpio = telefono.replace(Regex("[^0-9]"), "")
                        val url = "https://wa.me/$numeroLimpio"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AdBanner()
            }
        }
    }
}

@Composable
fun MascotaWhatsAppCard(
    mascota: Mascota,
    onWhatsAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (mascota.imagen != null) {
                            MascotaImagen(
                                imagen = mascota.imagen,
                                tipo = mascota.tipo,
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        } else {
                            Text(
                                text = if (mascota.tipo == "Perro") "🐶" else "🐱",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column {
                            Text(
                                text = mascota.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${mascota.edadMeses} meses",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                
                Button(
                    onClick = {
                        val telefono = mascota.dueno?.telefono ?: ""
                        if (telefono.isNotEmpty()) {
                            onWhatsAppClick(telefono)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Border)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (mascota.vacunas == "Si") "✅ Vacunado" else "❌ Sin vacunas",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            Text(
                text = "📍 ${mascota.direccion.ifEmpty { "No especificada" }}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            if (!mascota.descripcion.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📝 ${mascota.descripcion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            if (mascota.dueno != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val telefono = mascota.dueno?.telefono ?: ""
                                if (telefono.isNotEmpty()) {
                                    onWhatsAppClick(telefono)
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Contacto: ${mascota.dueno.nombre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackground,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Toca para chatear por WhatsApp",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MascotaImagen(
    imagen: String,
    tipo: String,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(imagen) {
        bitmap = try {
            if (imagen.startsWith("data:image")) {
                val base64Data = imagen.substringAfter("base64,")
                val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(SurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Foto de mascota",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: Text(
            text = if (tipo == "Perro") "🐶" else "🐱",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
