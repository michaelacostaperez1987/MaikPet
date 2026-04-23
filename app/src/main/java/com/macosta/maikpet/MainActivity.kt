package com.macosta.maikpet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        
        setContent {
            PetTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                
                // Verificar autenticación - si no hay usuario logueado, ir a login
                LaunchedEffect(uiState.isLoggedIn, uiState.currentUser) {
                    if (!uiState.isLoggedIn && uiState.currentUser == null) {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
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
                    onLocationUpdate = { },
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
                        // Redirigir a login si no está autenticado
                        LaunchedEffect(Unit) {
                            viewModel.navigateTo(Screen.Login)
                        }
                        // Mostrar pantalla de carga o mensaje
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Redirigiendo a login...")
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
                        // Redirigir a login si no está autenticado
                        LaunchedEffect(Unit) {
                            viewModel.navigateTo(Screen.Login)
                        }
                        // Mostrar pantalla de carga o mensaje
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Redirigiendo a login...")
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
                Screen.Login -> LoginScreen(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onLogin = { email, password -> viewModel.login(email, password) },
                    onRegister = { nombre, dir, tel, email, pass, edad ->
                        viewModel.register(nombre, dir, tel, email, pass, edad)
                    },
                    onViewPrivacy = { viewModel.navigateTo(Screen.Legal) },
                    onViewTerms = { viewModel.navigateTo(Screen.Legal) },
                    onClearError = { viewModel.clearError() }
                )
                Screen.Legal -> LegalScreen(
                    onBack = { viewModel.navigateTo(Screen.Login) }
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
            ) {
                // Perfil del usuario
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
                                text = usuario.email ?: "",
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
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Border)
                Spacer(modifier = Modifier.height(8.dp))
                
                DrawerItem(
                    icon = Icons.Default.Logout,
                    text = "Cerrar Sesión",
                    selected = false,
                    onClick = {
                        viewModel.logout()
                        drawerOpen = false
                    }
                )
                
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
                
                var showExitDialog by remember { mutableStateOf(false) }
                
                DrawerItem(
                    icon = Icons.Default.ExitToApp,
                    text = "Salir",
                    selected = false,
                    onClick = {
                        drawerOpen = false
                        showExitDialog = true
                    }
                )
                
                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = { Text("Salir de la aplicación") },
                        text = { Text("¿Estás seguro que querés salir? Se cerrará la sesión y la aplicación.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showExitDialog = false
                                    // 1. Cerrar sesión primero
                                    viewModel.logout()
                                    // 2. Cerrar completamente la aplicación
                                    val activity = context as? ComponentActivity
                                    activity?.runOnUiThread {
                                        // Método más efectivo para cerrar la app
                                        activity.finishAffinity()
                                        // Forzar cierre del proceso para asegurar
                                        android.os.Process.killProcess(android.os.Process.myPid())
                                    }
                                }
                            ) {
                                Text("Salir")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showExitDialog = false }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    )
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