package com.macosta.maikpet.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.macosta.maikpet.data.api.MaikPetApi
import com.macosta.maikpet.data.api.TokenRequest
import com.macosta.maikpet.data.api.UpdatePerfilRequest
import com.macosta.maikpet.data.model.*
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

@Singleton
class MaikPetRepository @Inject constructor(
    private val api: MaikPetApi,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private val USER_KEY = stringPreferencesKey("user_data")
        private val SESSION_TIMESTAMP = longPreferencesKey("session_timestamp")
        private val SESSION_EXPIRY_DAYS = 7
    }

    private suspend fun saveSessionTimestamp() {
        try {
            context.dataStore.edit { prefs ->
                prefs[SESSION_TIMESTAMP] = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Log.e("MaikPetRepo", "Error al guardar timestamp", e)
        }
    }

    private suspend fun getSessionTimestamp(): Long? {
        return try {
            context.dataStore.data.map { prefs ->
                prefs[SESSION_TIMESTAMP]
            }.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun isSessionExpired(): Boolean {
        val timestamp = getSessionTimestamp() ?: return true
        val diasTranscurridos = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60 * 24)
        return diasTranscurridos > SESSION_EXPIRY_DAYS
    }

    private suspend fun clearSessionTimestamp() {
        try {
            context.dataStore.edit { prefs ->
                prefs.remove(SESSION_TIMESTAMP)
            }
        } catch (e: Exception) {
            Log.e("MaikPetRepo", "Error al limpiar timestamp", e)
        }
    }

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser: StateFlow<Usuario?> = _currentUser.asStateFlow()
    
    private val _mascotas = MutableStateFlow<List<Mascota>>(emptyList())
    val mascotas: StateFlow<List<Mascota>> = _mascotas.asStateFlow()
    
    private val _misMascotas = MutableStateFlow<List<Mascota>>(emptyList())
    val misMascotas: StateFlow<List<Mascota>> = _misMascotas.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            loadSavedUser()
        }
    }

    private suspend fun loadSavedUser() {
        try {
            context.dataStore.data.collect { prefs ->
                val userJson = prefs[USER_KEY]
                if (userJson != null) {
                    try {
                        val user = gson.fromJson(userJson, Usuario::class.java)
                        val diasTranscurridos = (System.currentTimeMillis() - user.fechaRegistro) / (1000 * 60 * 60 * 24)
                        if (diasTranscurridos > 90) {
                            Log.d("MaikPetRepo", "Datos eliminados: usuario expirado (>90 días)")
                            clearUser()
                            clearSessionTimestamp()
                        } else if (isSessionExpired()) {
                            Log.d("MaikPetRepo", "Sesión expirada (>7 días)")
                            _currentUser.value = null
                            clearUser()
                            clearSessionTimestamp()
                        } else {
                            _currentUser.value = user
                            Log.d("MaikPetRepo", "Usuario cargado desde cache: ${user.nombre}")
                        }
                    } catch (e: Exception) {
                        Log.e("MaikPetRepo", "Error al parsear usuario guardado", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MaikPetRepo", "Error al cargar usuario guardado", e)
        }
    }

    private suspend fun saveUser(user: Usuario) {
        try {
            val userJson = gson.toJson(user)
            context.dataStore.edit { prefs ->
                prefs[USER_KEY] = userJson
                prefs[SESSION_TIMESTAMP] = System.currentTimeMillis()
            }
            Log.d("MaikPetRepo", "Usuario guardado en cache")
        } catch (e: Exception) {
            Log.e("MaikPetRepo", "Error al guardar usuario", e)
        }
    }

    private suspend fun clearUser() {
        try {
            context.dataStore.edit { prefs ->
                prefs.remove(USER_KEY)
            }
            Log.d("MaikPetRepo", "Usuario eliminado del cache")
        } catch (e: Exception) {
            Log.e("MaikPetRepo", "Error al eliminar usuario", e)
        }
    }
    
    suspend fun getMascotas(): Result<List<Mascota>> {
        return try {
            _isLoading.value = true
            val response = api.getMascotas()
            if (response.isSuccessful) {
                val mascotas = response.body() ?: emptyList()
                _mascotas.value = mascotas
                Result.Success(mascotas)
            } else {
                Result.Error("Error al cargar mascotas")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun getMisMascotas(): Result<List<Mascota>> {
        return try {
            _isLoading.value = true
            val response = api.getMisMascotas()
            if (response.isSuccessful) {
                val mascotas = response.body() ?: emptyList()
                _misMascotas.value = mascotas
                Result.Success(mascotas)
            } else {
                Result.Error("Error al cargar tus mascotas")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun addMascota(mascota: MascotaRequest): Result<Boolean> {
        return try {
            _isLoading.value = true
            val response = api.addMascota(mascota)
            android.util.Log.d("MaikPetRepo", "addMascota response code: ${response.code()}")
            android.util.Log.d("MaikPetRepo", "addMascota response: ${response.body()}")
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    getMisMascotas()
                    getMascotas()
                    Result.Success(true)
                } else {
                    Result.Error(body?.error ?: "Error al guardar")
                }
            } else {
                Result.Error("Error del servidor (${response.code()})")
            }
        } catch (e: Exception) {
            android.util.Log.e("MaikPetRepo", "addMascota error", e)
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteMascota(id: Int): Result<Boolean> {
        return try {
            _isLoading.value = true
            val response = api.deleteMascota(DeleteRequest(id))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    getMisMascotas()
                    getMascotas()
                    Result.Success(true)
                } else {
                    Result.Error(body?.error ?: "Error al eliminar")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateMascota(id: Int, request: MascotaRequest): Result<Boolean> {
        return try {
            _isLoading.value = true
            val requestWithId = MascotaRequest(
                id = id,
                nombre = request.nombre,
                tipo = request.tipo,
                edadMeses = request.edadMeses,
                vacunas = request.vacunas,
                direccion = request.direccion,
                descripcion = request.descripcion,
                lat = request.lat,
                lng = request.lng,
                imagen = request.imagen
            )
            val response = api.updateMascota(requestWithId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    getMisMascotas()
                    getMascotas()
                    Result.Success(true)
                } else {
                    Result.Error(body?.error ?: "Error al actualizar")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updatePerfil(id: Int, nombre: String, direccion: String, telefono: String): Result<Boolean> {
        return try {
            _isLoading.value = true
            val response = api.updatePerfil(UpdatePerfilRequest(id, nombre, direccion, telefono))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    val updatedUser = _currentUser.value?.copy(nombre = nombre, direccion = direccion, telefono = telefono)
                    _currentUser.value = updatedUser
                    updatedUser?.let { saveUser(it) }
                    Result.Success(true)
                } else {
                    Result.Error(body?.error ?: "Error al actualizar")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            _isLoading.value = true
            _error.value = null
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.usuario != null) {
                    _currentUser.value = body.usuario
                    saveUser(body.usuario)
                    Result.Success(body.usuario)
                } else {
                    val msg = body?.error ?: "Error al iniciar sesión"
                    _error.value = msg
                    Result.Error(msg)
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Error de conexión"
            _error.value = msg
            Result.Error(msg)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun sendDeviceToken(token: String): Boolean {
        return try {
            Log.d("MaikPetRepo", "Enviando token: $token")
            val response = api.saveDeviceToken(TokenRequest(token))
            Log.d("MaikPetRepo", "Token response: ${response.code()}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MaikPetRepo", "Error al enviar token: ${e.message}")
            false
        }
    }
    
    suspend fun register(nombre: String, direccion: String, telefono: String, email: String, password: String, edad: Int): Result<Boolean> {
        if (edad < 18) {
            return Result.Error("Debes tener al menos 18 años para registrarte")
        }
        return try {
            _isLoading.value = true
            val response = api.register(RegisterRequest(nombre, direccion, telefono, email, password, edad))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.Success(true)
                } else {
                    Result.Error(body?.error ?: "Error al registrarse")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun logout(): Result<Boolean> {
        return try {
            api.logout()
            _currentUser.value = null
            _misMascotas.value = emptyList()
            clearUser()
            clearSessionTimestamp()
            Result.Success(true)
        } catch (e: Exception) {
            _currentUser.value = null
            _misMascotas.value = emptyList()
            clearUser()
            clearSessionTimestamp()
            Result.Success(true)
        }
    }
    
    suspend fun checkSession(): Result<Usuario?> {
        return try {
            val response = api.getSession()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.logueado == true && body.usuario != null) {
                    _currentUser.value = body.usuario
                    saveUser(body.usuario)
                    Result.Success(body.usuario)
                } else {
                    val cachedUser = _currentUser.value
                    if (cachedUser != null) {
                        Result.Success(cachedUser)
                    } else {
                        _currentUser.value = null
                        Result.Success(null)
                    }
                }
            } else {
                val cachedUser = _currentUser.value
                if (cachedUser != null) {
                    Result.Success(cachedUser)
                } else {
                    Result.Success(null)
                }
            }
        } catch (e: Exception) {
            val cachedUser = _currentUser.value
            if (cachedUser != null) {
                Log.d("MaikPetRepo", "Usando usuario cache por error de conexión")
                Result.Success(cachedUser)
            } else {
                Result.Success(null)
            }
        }
    }
}
