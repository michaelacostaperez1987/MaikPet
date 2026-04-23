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
        private val MASCOTAS_CACHE = stringPreferencesKey("mascotas_cache")
        private val CACHE_TIMESTAMP = longPreferencesKey("cache_timestamp")
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 horas
        private const val SESSION_PREFS = "session_prefs"
        private const val SESSION_ID_KEY = "session_id"
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val CACHED_USER_ID = "cached_user_id"
    }

    private val sessionPrefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)

    private fun saveSessionId(sessionId: String) {
        sessionPrefs.edit().putString(SESSION_ID_KEY, sessionId).apply()
    }

    private fun getSessionId(): String? {
        return sessionPrefs.getString(SESSION_ID_KEY, null)
    }

    private fun saveAuthToken(token: String) {
        sessionPrefs.edit().putString(AUTH_TOKEN_KEY, token).apply()
    }

    private fun getAuthToken(): String? {
        return sessionPrefs.getString(AUTH_TOKEN_KEY, null)
    }

    private fun saveCachedUserId(userId: Int) {
        sessionPrefs.edit().putInt(CACHED_USER_ID, userId).apply()
        Log.d("MaikPetRepo", "Saved userId: $userId")
    }

    private fun getCachedUserId(): Int {
        return sessionPrefs.getInt(CACHED_USER_ID, -1)
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
        // Intentar cargar desde cache primero
        val cachedMascotas = getCachedMascotas()
        val hasCache = cachedMascotas.isNotEmpty()
        if (hasCache) {
            _mascotas.value = cachedMascotas
        }
        
        return try {
            _isLoading.value = true
            val response = api.getMascotas()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    val mascotas = body.mascotas ?: emptyList()
                    _mascotas.value = mascotas
                    cacheMascotas(mascotas)
                    Result.Success(mascotas)
                } else {
                    Result.Error(body?.error ?: "Error al cargar mascotas")
                }
            } else {
                val errorMsg = when (response.code()) {
                    500 -> "Error del servidor. Intenta más tarde"
                    401 -> "Sesión expirada"
                    404 -> "No se encontraron mascotas"
                    else -> "Error al cargar mascotas"
                }
                Result.Error(if (hasCache) "Sin conexión. Mostrando datos guardados." else errorMsg)
            }
        } catch (e: java.net.UnknownHostException) {
            Result.Error("Sin conexión. Mostrando datos guardados.")
        } catch (e: java.net.SocketTimeoutException) {
            Result.Error("Tiempo de espera agotado. Intenta de nuevo.")
        } catch (e: retrofit2.HttpException) {
            Result.Error("Error de conexión (${e.code()})")
        } catch (e: Exception) {
            Result.Error("Error de conexión")
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun getCachedMascotas(): List<Mascota> {
        return try {
            val timestamp = context.dataStore.data.first()[CACHE_TIMESTAMP] ?: 0L
            if (System.currentTimeMillis() - timestamp > CACHE_DURATION_MS) {
                return emptyList() // Cache expirado
            }
            val cached = context.dataStore.data.first()[MASCOTAS_CACHE]
            if (cached != null) {
                gson.fromJson(cached, object : com.google.gson.reflect.TypeToken<List<Mascota>>() {}.type)
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private suspend fun cacheMascotas(mascotas: List<Mascota>) {
        try {
            context.dataStore.edit { prefs ->
                prefs[MASCOTAS_CACHE] = gson.toJson(mascotas)
                prefs[CACHE_TIMESTAMP] = System.currentTimeMillis()
            }
        } catch (e: Exception) { Log.e("MaikPetRepo", "Error cacheando", e) }
    }
    
    suspend fun getMisMascotas(): Result<List<Mascota>> {
        return try {
            _isLoading.value = true
            val response = api.getMisMascotas()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    val mascotas = body.mascotas ?: emptyList()
                    _misMascotas.value = mascotas
                    Result.Success(mascotas)
                } else {
                    Result.Error(body?.error ?: "Error al cargar tus mascotas")
                }
            } else {
                Result.Error("Error del servidor (${response.code()})")
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
                    Log.d("MaikPetRepo", "Perfil actualizado")
                    Result.Success(true)
                } else {
                    Result.Error(body?.error ?: "Error al actualizar")
                }
            } else {
                Result.Error("Error del servidor")
            }
        } catch (e: Exception) {
            Log.e("MaikPetRepo", "Error al actualizar perfil", e)
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
                    // Guardar sesión
                    body.sessionId?.let { 
                        saveSessionId(it)
                        Log.d("MaikPetRepo", "Saved sessionId: $it")
                    }
                    body.token?.let { 
                        saveAuthToken(it)
                        Log.d("MaikPetRepo", "Saved token: $it")
                    }
                    saveCachedUserId(body.usuario.id)
                    Log.d("MaikPetRepo", "Saved userId: ${body.usuario.id}")
                    Result.Success(body.usuario)
                } else {
                    val msg = when {
                        body?.error?.contains("email") == true -> "Email o contraseña incorrectos"
                        body?.error?.contains("password") == true -> "Email o contraseña incorrectos"
                        else -> body?.error ?: "Credenciales incorrectas"
                    }
                    _error.value = msg
                    Result.Error(msg)
                }
            } else {
                val errorMsg = when (response.code()) {
                    500 -> "Error del servidor"
                    401 -> "Credenciales incorrectas"
                    else -> "No se pudo iniciar sesión"
                }
                Result.Error(errorMsg)
            }
        } catch (e: java.net.UnknownHostException) {
            val msg = "Sin conexión a internet"
            _error.value = msg
            Result.Error(msg)
        } catch (e: java.net.SocketTimeoutException) {
            val msg = "Tiempo de espera agotado"
            _error.value = msg
            Result.Error(msg)
        } catch (e: retrofit2.HttpException) {
            val msg = when (e.code()) {
                401 -> "Credenciales incorrectas"
                500 -> "Error del servidor"
                else -> "Error de conexión"
            }
            _error.value = msg
            Result.Error(msg)
        } catch (e: Exception) {
            val msg = "Verifica tu conexión a internet"
            _error.value = msg
            Result.Error(msg)
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun sendDeviceToken(token: String): Boolean {
        val userId = currentUser.value?.id ?: return false
        return try {
            Log.d("MaikPetRepo", "Enviando token para user: $userId")
            val response = api.saveDeviceToken(TokenRequest(token, userId))
            Log.d("MaikPetRepo", "Token response: ${response.code()}")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MaikPetRepo", "Error al enviar token: ${e.message}")
            false
        }
    }
    
suspend fun register(nombre: String, direccion: String, telefono: String, email: String, password: String, edad: Int): Result<Usuario> {
        if (nombre.isBlank()) return Result.Error("Ingresa tu nombre")
        if (!isValidEmail(email)) return Result.Error("Email invalido")
        if (password.length < 4) return Result.Error("Contrasena minimo 4 caracteres")
        
        return try {
            _isLoading.value = true
            val response = api.register(RegisterRequest(nombre, direccion, telefono, email, password, edad))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.usuario != null) {
                    _currentUser.value = body.usuario
                    saveUser(body.usuario)
                    // Guardar sesión
                    body.sessionId?.let { saveSessionId(it) }
                    body.token?.let { saveAuthToken(it) }
                    saveCachedUserId(body.usuario.id)
                    Result.Success(body.usuario)
                } else {
                    val msg = when {
                        body?.error?.contains("email") == true -> "Email yaestá registrado"
                        else -> body?.error ?: "No se pudo completar el registro"
                    }
                    Result.Error(msg)
                }
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "Email ya está registrado"
                    500 -> "Error del servidor"
                    else -> "No se pudo completar el registro"
                }
                Result.Error(errorMsg)
            }
        } catch (e: java.net.UnknownHostException) {
            Result.Error("Sin conexión a internet")
        } catch (e: Exception) {
            Result.Error("Verifica tu conexión")
        } finally {
            _isLoading.value = false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
