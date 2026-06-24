package com.cibertec.denticoremobile.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para enviar las credenciales al endpoint de autenticación.
 */
data class LoginRequest(
    @SerializedName("dni")
    val dni: String,

    @SerializedName("password")
    val password: String
)

/**
 * DTO con la respuesta del backend tras un login exitoso.
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("rol")
    val rol: String
)
