package com.macosta.maikpet.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.macosta.maikpet.data.model.Mascota
import com.macosta.maikpet.ui.theme.*
import com.macosta.maikpet.util.ImageUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@Composable
fun MapaScreen(
    mascotas: List<Mascota>,
    onRefresh: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val defaultLocation = LatLng(-34.9011, -56.1645)
    
    var userLocation by remember { mutableStateOf(defaultLocation) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var selectedMascota by remember { mutableStateOf<Mascota?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val mascotasConUbicacion = mascotas.filter { it.lat != null && it.lng != null }
    
    val initialPosition = remember(mascotasConUbicacion) {
        if (mascotasConUbicacion.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            mascotasConUbicacion.forEach { mascota ->
                boundsBuilder.include(LatLng(mascota.lat!!, mascota.lng!!))
            }
            try {
                val bounds = boundsBuilder.build()
                CameraPosition.fromLatLngZoom(bounds.center, 12f)
            } catch (e: Exception) {
                CameraPosition.fromLatLngZoom(defaultLocation, 11f)
            }
        } else {
            CameraPosition.fromLatLngZoom(defaultLocation, 11f)
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = initialPosition
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation(context) { location ->
                userLocation = location
                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 14f)
            }
        }
    }
    
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            getCurrentLocation(context) { location ->
                userLocation = location
            }
        }
    }
    
    LaunchedEffect(isLoading) {
        if (!isLoading && isRefreshing) {
            kotlinx.coroutines.delay(500)
            isRefreshing = false
        }
    }
    
    LaunchedEffect(mascotasConUbicacion.size) {
        if (mascotasConUbicacion.isNotEmpty()) {
            try {
                val boundsBuilder = LatLngBounds.Builder()
                mascotasConUbicacion.forEach { mascota ->
                    boundsBuilder.include(LatLng(mascota.lat!!, mascota.lng!!))
                }
                val bounds = boundsBuilder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 150),
                    durationMs = 1000
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .clickable { if (selectedMascota != null) selectedMascota = null },
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = hasLocationPermission,
                compassEnabled = true,
                mapToolbarEnabled = true
            )
        ) {
            mascotasConUbicacion.forEach { mascota ->
                val position = LatLng(mascota.lat!!, mascota.lng!!)
                val isPerro = mascota.tipo == "Perro"
                
                Marker(
                    state = MarkerState(position = position),
                    title = "${if (isPerro) "🐶" else "🐱"} ${mascota.nombre}",
                    snippet = "${mascota.edadMeses} meses - ${if (mascota.vacunas == "Si") "💉" else "❌"}\n${mascota.direccion}",
                    icon = createMascotaBitmapDescriptor(context, isPerro),
                    onClick = {
                        selectedMascota = mascota
                        true
                    }
                )
            }
        }
        
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${mascotasConUbicacion.size} mascotas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnBackground,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        selectedMascota?.let { mascota ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .padding(top = 70.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            MapaMascotaImagen(
                                imagen = mascota.imagen,
                                tipo = mascota.tipo,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = mascota.nombre,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${mascota.edadMeses} meses - ${if (mascota.vacunas == "Si") "💉 Vacunado" else "❌ Sin vacunas"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        IconButton(onClick = { selectedMascota = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Border)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "📍 ${mascota.direccion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnBackground
                    )
                    if (mascota.dueno != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📞 ${mascota.dueno.telefono ?: mascota.dueno.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary
                        )
                    }
                    if (!mascota.descripcion.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📝 ${mascota.descripcion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toca el mapa para cerrar",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        
        if (hasLocationPermission) {
            FloatingActionButton(
                onClick = {
                    getCurrentLocation(context) { location ->
                        userLocation = location
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp),
                containerColor = Primary,
                contentColor = OnPrimary,
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Mi ubicación"
                )
            }
        }
        
        FloatingActionButton(
            onClick = {
                isRefreshing = true
                onRefresh()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = Surface,
            contentColor = Primary,
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar"
                )
            }
        }
        
        if (mascotasConUbicacion.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = 60.dp),
                colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "No hay mascotas con ubicación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

private fun createMascotaBitmapDescriptor(context: Context, isPerro: Boolean): BitmapDescriptor {
    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val emoji = if (isPerro) "🐶" else "🐱"
    
    val bgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#16213E")
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, bgPaint)
    
    val borderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#00ADB5")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, borderPaint)
    
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    
    val fontMetrics = textPaint.fontMetrics
    val textY = size / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(emoji, size / 2f, textY, textPaint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Suppress("MissingPermission")
private fun getCurrentLocation(context: Context, onLocationReceived: (LatLng) -> Unit) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                onLocationReceived(LatLng(location.latitude, location.longitude))
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

@Composable
private fun MapaMascotaImagen(
    imagen: String?,
    tipo: String,
    modifier: Modifier = Modifier
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(imagen) {
        bitmap = imagen?.let { ImageUtils.decodeBase64ToBitmap(it) }
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
            fontSize = 28.sp
        )
    }
}
