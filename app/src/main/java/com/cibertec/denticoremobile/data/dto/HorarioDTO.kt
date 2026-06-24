package com.cibertec.denticoremobile.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO que representa un cupo/horario disponible para agendar una cita.
 * Contrato asumido: GET /api/v1/citas/disponibilidad?fecha={fecha}
 */
data class HorarioDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("fechaHora")
    val fechaHora: String,

    @SerializedName("idOdontologo")
    val idOdontologo: Int,

    @SerializedName("nombreOdontologo")
    val nombreOdontologo: String,

    @SerializedName("especialidad")
    val especialidad: String,

    @SerializedName("disponible")
    val disponible: Boolean
)
