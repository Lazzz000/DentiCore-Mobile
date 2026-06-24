package com.cibertec.denticoremobile.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO básico con la información clínica del paciente autenticado.
 * Contrato asumido: GET /api/v1/historial/paciente
 */
data class HistorialDTO(
    @SerializedName("idPaciente")
    val idPaciente: Int,

    @SerializedName("codigoHistorial")
    val codigoHistorial: String,

    @SerializedName("nombres")
    val nombres: String,

    @SerializedName("apellidos")
    val apellidos: String,

    @SerializedName("atenciones")
    val atenciones: List<AtencionDTO> = emptyList()
)

/**
 * Representa una atención clínica dentro del historial del paciente.
 */
data class AtencionDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("fechaAtencion")
    val fechaAtencion: String,

    @SerializedName("motivoConsulta")
    val motivoConsulta: String,

    @SerializedName("notasClinicas")
    val notasClinicas: String? = null,

    @SerializedName("odontograma")
    val odontograma: OdontogramaDTO? = null
)

/**
 * Representación simplificada del odontograma asociado a una atención.
 */
data class OdontogramaDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("tipo")
    val tipo: String,

    @SerializedName("detalles")
    val detalles: List<DetalleOdontogramaDTO> = emptyList()
)

/**
 * Detalle de una pieza dental dentro del odontograma.
 */
data class DetalleOdontogramaDTO(
    @SerializedName("numeroPieza")
    val numeroPieza: Int,

    @SerializedName("diagnostico")
    val diagnostico: String,

    @SerializedName("estadoTratamiento")
    val estadoTratamiento: String
)
