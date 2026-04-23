package com.macosta.maikpet.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.macosta.maikpet.data.model.Mascota
import com.macosta.maikpet.data.model.Usuario
import com.macosta.maikpet.ui.theme.*
import com.macosta.maikpet.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaikPetDrawer(
    currentScreen: Screen,
    currentUser: Usuario?,
    isLoggedIn: Boolean,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = SurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(SurfaceVariant, Background)
                        )
                    )
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
            ) {
                Column {
                    Text(
                        text = "Menu Principal",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentUser != null) {
                            "${currentUser.nombre}\n${currentUser.email}"
                        } else {
                            "No logueado"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackground.copy(alpha = 0.8f)
                    )
                }
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    DrawerItem(
                        icon = Icons.Default.Map,
                        title = "Mapa",
                        isActive = currentScreen == Screen.Mapa,
                        onClick = { onNavigate(Screen.Mapa) }
                    )
                }
                item {
                    DrawerItem(
                        icon = Icons.Default.Pets,
                        title = "Adopcion de animales",
                        isActive = currentScreen == Screen.Adopcion,
                        onClick = { onNavigate(Screen.Adopcion) }
                    )
                }
                if (isLoggedIn) {
                    item {
                        DrawerItem(
                            icon = Icons.Default.Favorite,
                            title = "Mis Mascotas",
                            isActive = currentScreen == Screen.MisMascotas,
                            onClick = { onNavigate(Screen.MisMascotas) }
                        )
                    }
                    item {
                        DrawerItem(
                            icon = Icons.Default.AddCircle,
                            title = "Dar en adopcion",
                            isActive = currentScreen == Screen.DarAdopcion,
                            onClick = { onNavigate(Screen.DarAdopcion) }
                        )
                    }
                    item {
                        DrawerItem(
                            icon = Icons.Default.Logout,
                            title = "Cerrar Sesion",
                            isActive = false,
                            onClick = onLogout
                        )
                    }
                } else {
                    // Login/Registro ahora se maneja con LoginActivity separada
                    // No mostramos item de login en el drawer
                }
                item {
                    DrawerItem(
                        icon = Icons.Default.Info,
                        title = "Acerca de",
                        isActive = currentScreen == Screen.AcercaDe,
                        onClick = { onNavigate(Screen.AcercaDe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) OnPrimary.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent
    val contentColor = if (isActive) OnBackground else TextSecondary
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isActive) Primary else contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

@Composable
fun MascotaCard(
    mascota: Mascota,
    showDelete: Boolean = false,
    showExpireInfo: Boolean = false,
    showEdit: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (mascota.expira && showExpireInfo) {
        ErrorRed.copy(alpha = 0.1f)
    } else {
        CardBackground
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mascota.imagen != null) {
                        MascotaCardImage(
                            imagen = mascota.imagen,
                            tipo = mascota.tipo,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Text(
                            text = if (mascota.tipo == "Perro") "🐶" else "🐱",
                            fontSize = 24.sp
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
                        if (showExpireInfo && mascota.diasRestantes != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            val diasText = when {
                                mascota.diasRestantes <= 0 -> "⏰ Expirada"
                                mascota.diasRestantes <= 7 -> "⏰ ${mascota.diasRestantes}d restantes"
                                else -> "⏰ ${mascota.diasRestantes} días"
                            }
                            Text(
                                text = diasText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (mascota.expira) ErrorRed else TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📅 ${mascota.edadMeses} meses",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
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
                    Text(
                        text = "📝 ${mascota.descripcion.take(50)}${if (mascota.descripcion.length > 50) "..." else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (showExpireInfo && mascota.diasRestantes != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val diasText = when {
                        mascota.diasRestantes <= 0 -> "⏰ Esta publicación ha expirado"
                        mascota.diasRestantes <= 7 -> "⏰ Expira en ${mascota.diasRestantes} días"
                        else -> "⏰ ${mascota.diasRestantes} días restantes"
                    }
                    Text(
                        text = diasText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (mascota.expira) ErrorRed else Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (!showDelete && mascota.dueno != null) {
                    Text(
                        text = "📞 Contacto: ${mascota.dueno.telefono}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary
                    )
                }
            }
            Column {
                if (showEdit && onEdit != null) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = OnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (showDelete && onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(50))
                            .background(DeleteRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = OnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MascotaCardImage(
    imagen: String,
    tipo: String,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(imagen) {
        bitmap = try {
            if (imagen.startsWith("data:image")) {
                val base64Data = imagen.substringAfter("base64,")
                val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
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
                contentDescription = "Foto",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: Text(
            text = if (tipo == "Perro") "🐶" else "🐱",
            fontSize = 24.sp
        )
    }
}

@Composable
fun MascotaImagenUrl(
    imagen: String,
    tipo: String,  // Parámetro no usado pero mantenido por compatibilidad
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(imagen) {
        if (imagen.startsWith("data:image")) {
            try {
                val base64Data = imagen.substringAfter("base64,")
                val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } catch (e: Exception) {
                bitmap = null
            }
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
                contentDescription = "Foto",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: coil.compose.AsyncImage(
            model = if (imagen.startsWith("http")) imagen else "https://lmcosturas.com/pet/$imagen",
            contentDescription = "Foto",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Background.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun LoadingIndicator(message: String = "Cargando...") {
    Box(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
