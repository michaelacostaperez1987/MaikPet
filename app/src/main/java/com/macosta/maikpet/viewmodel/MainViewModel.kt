package com.macosta.maikpet.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.macosta.maikpet.data.model.MascotaRequest
import com.macosta.maikpet.data.model.Usuario
import com.macosta.maikpet.data.repository.GeocodingRepository
import com.macosta.maikpet.data.repository.MaikPetRepository
import com.macosta.maikpet.data.repository.Result
import com.macosta.maikpet.firebase.MaikPetFirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val currentUser: Usuario? = null,
    val mascotas: List<com.macosta.maikpet.data.model.Mascota> = emptyList(),
    val misMascotas: List<com.macosta.maikpet.data.model.Mascota> = emptyList(),
    val isLoading: Boolean = false,
    val isGeocoding: Boolean = false,
    val geocodingMessage: String? = null,
    val error: String? = null,
    val toastMessage: String? = null,
    val currentScreen: Screen = Screen.Mapa,
    val selectedMascota: com.macosta.maikpet.data.model.Mascota? = null,
    val showHome: Boolean = false
)

enum class Screen {
    Home, Mapa, Adopcion, MisMascotas, DarAdopcion, AcercaDe, Terminos, EditarMascota, EditarPerfil
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MaikPetRepository,
    private val geocodingRepository: GeocodingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val auth = Firebase.auth
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        checkSession()
        observeRepository()
    }
    
    private fun observeRepository() {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user, isLoggedIn = user != null) }
            }
        }
        viewModelScope.launch {
            repository.mascotas.collect { mascotas ->
                _uiState.update { it.copy(mascotas = mascotas) }
            }
        }
        viewModelScope.launch {
            repository.misMascotas.collect { misMascotas ->
                _uiState.update { it.copy(misMascotas = misMascotas) }
            }
        }
        viewModelScope.launch {
            repository.isLoading.collect { isLoading ->
                _uiState.update { it.copy(isLoading = isLoading) }
            }
        }
        viewModelScope.launch {
            repository.error.collect { error ->
                _uiState.update { it.copy(error = error) }
            }
        }
    }
    
    private fun checkSession() {
        viewModelScope.launch {
            when (val result = repository.checkSession()) {
                is com.macosta.maikpet.data.repository.Result.Success -> {
                    val user = result.data
                    if (user != null) {
                        _uiState.update { it.copy(currentUser = user, isLoggedIn = true) }
                        // Enviar token FCM
                        val fcmToken = MaikPetFirebaseService.getToken(context)
                        if (fcmToken != null) repository.sendDeviceToken(fcmToken)
                        loadMisMascotas()
                    } else {
                        _uiState.update { it.copy(isLoggedIn = false) }
                    }
                    loadMascotas()
                }
                is com.macosta.maikpet.data.repository.Result.Error -> {
                    _uiState.update { it.copy(isLoggedIn = false) }
                    loadMascotas()
                }
                is com.macosta.maikpet.data.repository.Result.Loading -> {}
            }
        }
    }
    
    fun loadMascotas() {
        viewModelScope.launch {
            repository.getMascotas()
        }
    }
    
    fun loadMisMascotas() {
        viewModelScope.launch {
            repository.getMisMascotas()
        }
    }
    
    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen, selectedMascota = null) }
        when (screen) {
            Screen.Mapa, Screen.Adopcion -> loadMascotas()
            Screen.MisMascotas -> loadMisMascotas()
            else -> {}
        }
    }
    
    fun editMascota(mascota: com.macosta.maikpet.data.model.Mascota) {
        _uiState.update { it.copy(selectedMascota = mascota, currentScreen = Screen.EditarMascota) }
    }
    
    fun updateMascota(nombre: String, tipo: String, edadMeses: Int, vacunas: String, direccion: String, descripcion: String, imagenBase64: String?) {
        val mascotaId = _uiState.value.selectedMascota?.id ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGeocoding = true, isLoading = true, geocodingMessage = "Actualizando...") }
            
            val location = geocodingRepository.geocodeAddress(direccion)
            
            if (location != null) {
                val request = MascotaRequest(
                    nombre = nombre,
                    tipo = tipo,
                    edadMeses = edadMeses,
                    vacunas = vacunas,
                    direccion = direccion,
                    descripcion = descripcion,
                    lat = location.latitude,
                    lng = location.longitude,
                    imagen = imagenBase64
                )
                
                when (val result = repository.updateMascota(mascotaId, request)) {
                    is Result.Success -> {
                        loadMascotas()
                        loadMisMascotas()
                        _uiState.update { it.copy(
                            toastMessage = "$nombre ha sido actualizada",
                            currentScreen = Screen.MisMascotas,
                            selectedMascota = null
                        )}
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    is Result.Loading -> {}
                }
            } else {
                _uiState.update { it.copy(
                    error = "No se pudo encontrar la ubicación"
                )}
            }
            
            _uiState.update { it.copy(isGeocoding = false, isLoading = false, geocodingMessage = null) }
        }
    }
    
    fun deleteMascotaFromEdit() {
        val mascotaId = _uiState.value.selectedMascota?.id ?: return
        deleteMascota(mascotaId)
    }

    fun updatePerfil(nombre: String, direccion: String, telefono: String) {
        val currentUser = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.updatePerfil(currentUser.id, nombre, direccion, telefono)) {
                is Result.Success -> {
                    val updatedUser = currentUser.copy(nombre = nombre, direccion = direccion, telefono = telefono)
                    _uiState.update { it.copy(
                        currentUser = updatedUser,
                        isLoading = false,
                        toastMessage = "Perfil actualizado correctamente"
                    )}
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Complete todos los campos") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.login(email, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, currentUser = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun register(nombre: String, direccion: String, telefono: String, email: String, password: String, edad: Int) {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Complete los campos obligatorios") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = repository.register(nombre, direccion, telefono, email, password, edad)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    // Auto login después de registro
                    login(email, password)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(currentUser = null, isLoggedIn = false) }
        }
    }
    
    fun addMascota(nombre: String, tipo: String, edadMeses: Int, vacunas: String, direccion: String, descripcion: String, imagenBase64: String? = null) {
        if (_uiState.value.currentUser == null) {
            _uiState.update { it.copy(error = "Debes iniciar sesión") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGeocoding = true, isLoading = true, error = null, geocodingMessage = "Buscando ubicación...") }
            
            val location = geocodingRepository.geocodeAddress(direccion)
            
            if (location != null) {
                _uiState.update { it.copy(geocodingMessage = "Ubicación encontrada! Guardando...") }
                
                val request = MascotaRequest(
                    nombre = nombre,
                    tipo = tipo,
                    edadMeses = edadMeses,
                    vacunas = vacunas,
                    direccion = direccion,
                    descripcion = descripcion,
                    lat = location.latitude,
                    lng = location.longitude,
                    imagen = imagenBase64
                )
                
                when (val result = repository.addMascota(request)) {
                    is Result.Success -> {
                        loadMascotas()
                        _uiState.update { it.copy(
                            toastMessage = "$nombre ha sido publicada para adopción",
                            currentScreen = Screen.MisMascotas
                        )}
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    is Result.Loading -> {}
                }
            } else {
                _uiState.update { it.copy(
                    error = "No se pudo encontrar la ubicación. Intenta con una dirección más específica."
                )}
            }
            
            _uiState.update { it.copy(isGeocoding = false, isLoading = false, geocodingMessage = null) }
        }
    }
    
    fun deleteMascota(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.deleteMascota(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(toastMessage = "Mascota eliminada") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                is Result.Loading -> {}
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun hideHome() {
        _uiState.update { it.copy(showHome = false) }
    }
}