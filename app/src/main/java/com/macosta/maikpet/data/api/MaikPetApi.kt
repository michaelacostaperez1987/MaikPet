package com.macosta.maikpet.data.api

import com.macosta.maikpet.data.model.ApiResponse
import com.macosta.maikpet.data.model.Mascota
import com.macosta.maikpet.data.model.MascotaRequest
import com.macosta.maikpet.data.model.SessionResponse
import com.macosta.maikpet.data.model.Usuario
import com.macosta.maikpet.data.model.DeleteRequest
import com.macosta.maikpet.data.model.LoginRequest
import com.macosta.maikpet.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Path

interface MaikPetApi {
    
    @GET("get_mascotas.php")
    suspend fun getMascotas(): Response<List<Mascota>>
    
    @GET("get_mis_mascotas.php")
    suspend fun getMisMascotas(): Response<List<Mascota>>
    
    @POST("add_mascota.php")
    suspend fun addMascota(@Body mascota: MascotaRequest): Response<ApiResponse<Mascota>>
    
    @HTTP(method = "DELETE", path = "delete_mascota.php", hasBody = true)
    suspend fun deleteMascota(@Body request: DeleteRequest): Response<ApiResponse<Nothing>>
    
    @POST("update_mascota.php")
    suspend fun updateMascota(@Body request: MascotaRequest): Response<ApiResponse<Nothing>>
    
    @POST("login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<Usuario>>
    
    @POST("register.php")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Usuario>>
    
    @POST("logout.php")
    suspend fun logout(): Response<ApiResponse<Nothing>>
    
    @GET("get_session.php")
    suspend fun getSession(): Response<SessionResponse>
    
    @POST("save_device_token.php")
    suspend fun saveDeviceToken(@Body request: TokenRequest): Response<ApiResponse<Nothing>>
}

data class TokenRequest(
    val token: String
)
