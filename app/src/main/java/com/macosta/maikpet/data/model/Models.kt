package com.macosta.maikpet.data.model

import com.google.gson.annotations.SerializedName

data class Mascota(
    val id: Int = 0,
    val nombre: String = "",
    val tipo: String = "",
    @SerializedName("edad_meses")
    val edadMeses: Int = 0,
    val vacunas: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val imagen: String? = null,
    val dueno: Usuario? = null,
    @SerializedName("dias_restantes")
    val diasRestantes: Int? = null,
    val expira: Boolean = false
)

data class Usuario(
    val id: Int = 0,
    val nombre: String = "",
    val email: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val edad: Int = 0,
    @SerializedName("fecha_registro")
    val fechaRegistro: Long = System.currentTimeMillis()
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val email: String,
    val password: String,
    val edad: Int,
    @SerializedName("acepta_tyc")
    val aceptaTyC: Boolean = true,
    @SerializedName("version_tyc")
    val versionTyC: String = "1.0",
    @SerializedName("fecha_aceptacion")
    val fechaAceptacion: Long = System.currentTimeMillis()
)

data class MascotaRequest(
    val id: Int? = null,
    val nombre: String,
    val tipo: String,
    @SerializedName("edad_meses")
    val edadMeses: Int,
    val vacunas: String,
    val direccion: String,
    val descripcion: String,
    val lat: Double? = null,
    val lng: Double? = null,
    val imagen: String? = null
)

data class DeleteRequest(
    val id: Int
)

data class ApiResponse<T>(
    val success: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val id: Int? = null,
    val usuario: Usuario? = null,
    val mascotas: List<Mascota>? = null,
    val logueado: Boolean = false
)

data class SessionResponse(
    val logueado: Boolean = false,
    val usuario: Usuario? = null
)
