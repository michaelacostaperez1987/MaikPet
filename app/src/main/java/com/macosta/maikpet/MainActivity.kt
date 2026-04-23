package com.macosta.maikpet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.macosta.maikpet.ui.screens.*
import com.macosta.maikpet.ui.theme.*
import com.macosta.maikpet.viewmodel.MainViewModel
import com.macosta.maikpet.viewmodel.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notificaciones habilitadas", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestNotificationPermission()

        val isGuestFromIntent = intent.getBooleanExtra("isGuest", false)
        
        setContent {
            PetTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                // Inicializar sesión con el flag de invitado
                LaunchedEffect(Unit) {
                    viewModel.initSession(isGuest = isGuestFromIntent)
                }

                // Verificar autenticación - solo cuando el ViewModel esté inicializado
                LaunchedEffect(uiState.initialized, uiState.isLoggedIn, uiState.currentUser, uiState.isGuest) {
                    if (uiState.initialized && !uiState.isLoggedIn && uiState.currentUser == null && !uiState.isGuest) {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                }
                
                // Si es invitado, forzar pantalla de adopción al inicio
                LaunchedEffect(uiState.isGuest) {
                    if (uiState.isGuest) {
                        viewModel.navigateTo(Screen.Adopcion)
                    }
                }
                
                MaikPetApp(viewModel)
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@Composable
fun MaikPetApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    
    val uiState by viewModel.uiState.collectAsState()
    var drawerOpen by remember { mutableStateOf(false) }
    
    val currentUser = uiState.currentUser
    
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, "ERROR: $error", Toast.LENGTH_LONG).show()
            android.util.Log.e("MaikPetApp", "Error shown: $error")
            viewModel.clearError()
        }
    }
    
    BackHandler {
        if (uiState.isGuest) {
            if (uiState.currentScreen != Screen.Adopcion) {
                viewModel.navigateTo(Screen.Adopcion)
            } else {
                (context as? ComponentActivity)?.finish()
            }
        } else if (uiState.currentScreen != Screen.Mapa) {
            viewModel.navigateTo(Screen.Mapa)
        } else {
            (context as? ComponentActivity)?.finish()
        }
    }
    
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceGlass)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { drawerOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = OnBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "MaikPet - Adopciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground
                    )
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
                .navigationBarsPadding()
        ) {
            when (uiState.currentScreen) {
                Screen.Home -> HomeScreen(
                    onStartClick = { viewModel.navigateTo(Screen.Mapa) }
                )
                Screen.Mapa -> MapaScreen(
                    mascotas = uiState.mascotas,
                    onRefresh = { viewModel.loadMascotas() },
                    isLoading = uiState.isLoading
                )
                Screen.Adopcion -> AdopcionScreen(
                    mascotas = uiState.mascotas,
                    isLoading = uiState.isLoading
                )
                Screen.MisMascotas -> {
                    if (uiState.isLoggedIn) {
                        MisMascotasScreen(
                            mascotas = uiState.misMascotas,
                            isLoggedIn = uiState.isLoggedIn,
                            isLoading = uiState.isLoading,
                            onDeleteMascota = { id -> viewModel.deleteMascota(id) },
                            onEditMascota = { mascota -> viewModel.editMascota(mascota) }
                        )
                    } else {
                        // Mostrar mensaje de que necesita iniciar sesión
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Debes iniciar sesión para ver tus mascotas")
                        }
                    }
                }
                Screen.DarAdopcion -> {
                    if (uiState.isLoggedIn) {
                        DarAdopcionScreen(
                            isLoggedIn = uiState.isLoggedIn,
                            currentUserName = currentUser?.nombre,
                            isLoading = uiState.isLoading,
                            isGeocoding = uiState.isGeocoding,
                            geocodingMessage = uiState.geocodingMessage,
                            onSubmit = { nombre, tipo, edad, vacunas, direccion, descripcion, imagen ->
                                viewModel.addMascota(nombre, tipo, edad, vacunas, direccion, descripcion, imagen)
                            }
                        )
                    } else {
                        // Mostrar mensaje de que necesita iniciar sesión
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Debes iniciar sesión para dar en adopción")
                        }
                    }
                }
                Screen.AcercaDe -> AcercaDeScreen()
                Screen.Terminos -> TerminosScreen(
                    onBack = { viewModel.navigateTo(Screen.Mapa) }
                )
                Screen.EditarMascota -> uiState.selectedMascota?.let { mascota ->
                    EditarMascotaScreen(
                        mascota = mascota,
                        isLoading = uiState.isLoading,
                        isGeocoding = uiState.isGeocoding,
                        geocodingMessage = uiState.geocodingMessage,
                        onSave = { nombre, tipo, edad, visas, direccion, descripcion, imagen ->
                            viewModel.updateMascota(nombre, tipo, edad, visas, direccion, descripcion, imagen)
                        },
                        onDelete = { mascotaId ->
                            viewModel.deleteMascota(mascotaId)
                        },
                        onBack = { viewModel.navigateTo(Screen.MisMascotas) }
                    )
                }
                Screen.EditarPerfil -> EditarPerfilScreen(
                    currentUser = uiState.currentUser,
                    isLoading = uiState.isLoading,
                    onSave = { nombre, direccion, telefono ->
                        viewModel.updatePerfil(nombre, direccion, telefono)
                    },
                    onBack = { viewModel.navigateTo(Screen.Mapa) }
                )
                // LoginScreen no se muestra aquí - se maneja con LoginActivity separada
                // Screen.Login -> LoginScreen(...) - ELIMINADO
                Screen.Legal -> LegalScreen(
                    onBack = { viewModel.navigateTo(Screen.Mapa) }
                )
            }
        }
    }
    
    if (drawerOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OnBackground.copy(alpha = 0.5f))
                .clickable { drawerOpen = false }
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(Surface)
                    .clickable(enabled = false) { }
                    .padding(vertical = 24.dp)
                    .navigationBarsPadding()
            ) {
                // Perfil del usuario
                if (uiState.isGuest) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Invitado",
                            modifier = Modifier.size(48.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Invitado",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Explora adopciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        currentUser?.let { usuario ->
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.2f)),
                                tint = Primary
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = usuario.nombre.ifEmpty { "Usuario" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = usuario.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        } ?: run {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Usuario",
                                modifier = Modifier.size(48.dp),
                                tint = Primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (!uiState.isGuest) {
                    DrawerItem(
                        icon = Icons.Default.Map,
                        text = "Mapa",
                        selected = uiState.currentScreen == Screen.Mapa,
                        onClick = {
                            viewModel.navigateTo(Screen.Mapa)
                            drawerOpen = false
                        }
                    )
                }
                DrawerItem(
                    icon = Icons.Default.Pets,
                    text = "Ver Adopciones",
                    selected = uiState.currentScreen == Screen.Adopcion,
                    onClick = {
                        viewModel.navigateTo(Screen.Adopcion)
                        drawerOpen = false
                    }
                )
                if (!uiState.isGuest) {
                    DrawerItem(
                        icon = Icons.Default.Favorite,
                        text = "Dar en Adopción",
                        selected = uiState.currentScreen == Screen.DarAdopcion,
                        onClick = {
                            viewModel.navigateTo(Screen.DarAdopcion)
                            drawerOpen = false
                        }
                    )
                    DrawerItem(
                        icon = Icons.Default.Edit,
                        text = "Editar Perfil",
                        selected = uiState.currentScreen == Screen.EditarPerfil,
                        onClick = {
                            viewModel.navigateTo(Screen.EditarPerfil)
                            drawerOpen = false
                        }
                    )
                    DrawerItem(
                        icon = Icons.Default.List,
                        text = "Mis Mascotas",
                        selected = uiState.currentScreen == Screen.MisMascotas,
                        onClick = {
                            viewModel.navigateTo(Screen.MisMascotas)
                            drawerOpen = false
                        }
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (uiState.isGuest) {
                    DrawerItem(
                        icon = Icons.Default.Login,
                        text = "Iniciar Sesión",
                        selected = false,
                        onClick = {
                            drawerOpen = false
                            viewModel.loginFromGuest()
                            context.startActivity(Intent(context, LoginActivity::class.java))
                            (context as? ComponentActivity)?.finish()
                        }
                    )
                } else {
                    DrawerItem(
                        icon = Icons.Default.Logout,
                        text = "Cerrar Sesión",
                        selected = false,
                        onClick = {
                            viewModel.logout()
                            drawerOpen = false
                        }
                    )
                }
                
                DrawerItem(
                    icon = Icons.Default.Info,
                    text = "Acerca de",
                    selected = uiState.currentScreen == Screen.AcercaDe,
                    onClick = {
                        viewModel.navigateTo(Screen.AcercaDe)
                        drawerOpen = false
                    }
                )
                
                DrawerItem(
                    icon = Icons.Default.Description,
                    text = "Términos y Condiciones",
                    selected = uiState.currentScreen == Screen.Terminos,
                    onClick = {
                        viewModel.navigateTo(Screen.Terminos)
                        drawerOpen = false
                    }
                )
                
                HorizontalDivider(color = Border)
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Primary else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) Primary else OnBackground
        )
    }
}