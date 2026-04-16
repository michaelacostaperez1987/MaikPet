package com.macosta.maikpet

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
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
        setContent {
            PetTheme {
                MaikPetApp()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaikPetApp(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var drawerOpen by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, "ERROR: $error", Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    BackHandler {
        if (uiState.currentScreen != Screen.Mapa) {
            viewModel.navigateTo(Screen.Mapa)
        } else {
            (context as? ComponentActivity)?.finish()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.showHome) {
            HomeScreen(onStartClick = { viewModel.hideHome() })
        } else if (!uiState.isLoggedIn) {
            LoginScreen(
                isLoading = uiState.isLoading,
                error = uiState.error,
                onLogin = { email, password -> viewModel.login(email, password) },
                onRegister = { nombre, dir, tel, email, pass, edad ->
                    viewModel.register(nombre, dir, tel, email, pass, edad)
                },
                onClearError = { viewModel.clearError() }
            )
        } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "MaikPet - Adopciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { drawerOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = OnBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Surface,
                        titleContentColor = OnBackground
                    )
                )
            },
            containerColor = Background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when (uiState.currentScreen) {
                        Screen.Home -> HomeScreen(
                            onStartClick = { viewModel.navigateTo(Screen.Mapa) }
                        )
                        Screen.Mapa -> MapaScreen(
                            mascotas = uiState.mascotas,
                            onLocationUpdate = { },
                            onRefresh = { viewModel.loadMascotas() },
                            isLoading = uiState.isLoading
                        )
                        Screen.Adopcion -> AdopcionScreen(
                            mascotas = uiState.mascotas,
                            isLoading = uiState.isLoading
                        )
                        Screen.MisMascotas -> MisMascotasScreen(
                            mascotas = uiState.misMascotas,
                            isLoggedIn = uiState.isLoggedIn,
                            isLoading = uiState.isLoading,
                            onDeleteMascota = { id -> viewModel.deleteMascota(id) },
                            onEditMascota = { mascota -> viewModel.editMascota(mascota) }
                        )
                        Screen.DarAdopcion -> DarAdopcionScreen(
                            isLoggedIn = uiState.isLoggedIn,
                            currentUserName = uiState.currentUser?.nombre,
                            isLoading = uiState.isLoading,
                            isGeocoding = uiState.isGeocoding,
                            geocodingMessage = uiState.geocodingMessage,
                            onSubmit = { nombre, tipo, edad, vacunas, direccion, descripcion, imagen ->
                                viewModel.addMascota(nombre, tipo, edad, vacunas, direccion, descripcion, imagen)
                            }
                        )
                        Screen.Login -> LoginScreen(
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            onLogin = { email, password -> viewModel.login(email, password) },
                            onRegister = { nombre, dir, tel, email, pass, edad ->
                                viewModel.register(nombre, dir, tel, email, pass, edad)
                            },
                            onClearError = { viewModel.clearError() }
                        )
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
                                onSave = { nombre, tipo, edad, vacunas, direccion, descripcion, imagen ->
                                    viewModel.updateMascota(nombre, tipo, edad, vacunas, direccion, descripcion, imagen)
                                },
                                onBack = { viewModel.navigateTo(Screen.MisMascotas) }
                            )
                        }
                    }
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
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🐾",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MaikPet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text(
                                text = "Adopciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Border)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isLoggedIn) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
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
                                Column {
                                    Text(
                                        text = uiState.currentUser?.nombre ?: "Usuario",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = uiState.currentUser?.email ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    DrawerItem(
                        icon = Icons.Default.Map,
                        text = "Mapa",
                        selected = uiState.currentScreen == Screen.Mapa,
                        onClick = {
                            viewModel.navigateTo(Screen.Mapa)
                            drawerOpen = false
                        }
                    )
                    DrawerItem(
                        icon = Icons.Default.Pets,
                        text = "Ver Adopciones",
                        selected = uiState.currentScreen == Screen.Adopcion,
                        onClick = {
                            viewModel.navigateTo(Screen.Adopcion)
                            drawerOpen = false
                        }
                    )
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
                        icon = Icons.Default.List,
                        text = "Mis Mascotas",
                        selected = uiState.currentScreen == Screen.MisMascotas,
                        onClick = {
                            viewModel.navigateTo(Screen.MisMascotas)
                            drawerOpen = false
                        }
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(color = Border)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (uiState.isLoggedIn) {
                        DrawerItem(
                            icon = Icons.Default.Logout,
                            text = "Cerrar Sesión",
                            selected = false,
                            onClick = {
                                viewModel.logout()
                                drawerOpen = false
                            }
                        )
                    } else {
                        DrawerItem(
                            icon = Icons.Default.Login,
                            text = "Iniciar Sesión",
                            selected = uiState.currentScreen == Screen.Login,
                            onClick = {
                                viewModel.navigateTo(Screen.Login)
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
                    
                    DrawerItem(
                        icon = Icons.Default.ExitToApp,
                        text = "Salir",
                        selected = false,
                        onClick = {
                            drawerOpen = false
                            (context as? ComponentActivity)?.finish()
                        }
                    )
                }
            }
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
