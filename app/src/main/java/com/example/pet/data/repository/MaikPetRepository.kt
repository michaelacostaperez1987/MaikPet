package com.example.pet.data.repository

import com.example.pet.data.api.MaikPetApi
import com.example.pet.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

@Singleton
class MaikPetRepository @Inject constructor(
    private val api: MaikPetApi
) {
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
    
    suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            _isLoading.value = true
            _error.value = null
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.usuario != null) {
                    _currentUser.value = body.usuario
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
    
    suspend fun register(nombre: String, direccion: String, telefono: String, email: String, password: String): Result<Boolean> {
        return try {
            _isLoading.value = true
            val response = api.register(RegisterRequest(nombre, direccion, telefono, email, password))
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
            Result.Success(true)
        } catch (e: Exception) {
            _currentUser.value = null
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
                    Result.Success(body.usuario)
                } else {
                    _currentUser.value = null
                    Result.Success(null)
                }
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Success(null)
        }
    }
}
