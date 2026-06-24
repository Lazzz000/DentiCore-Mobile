package com.cibertec.denticoremobile.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para solicitar la reserva de una cita.
 * Contrato asumido: POST /api/v1/citas
 */
data class AgendarCitaRequest(
    @SerializedName("idPaciente")
    val idPaciente: Int,

    @SerializedName("idOdontologo")
    val idOdontologo: Int,

    @SerializedName("fechaHora")
    val fechaHora: String
)
