package com.cibertec.denticoremobile.data.remote

import com.cibertec.denticoremobile.data.dto.AgendarCitaRequest
import com.cibertec.denticoremobile.data.dto.HistorialDTO
import com.cibertec.denticoremobile.data.dto.HorarioDTO
import com.cibertec.denticoremobile.data.dto.LoginRequest
import com.cibertec.denticoremobile.data.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interfaz de Retrofit que define el contrato de servicios REST del BFF.
 * Todas las operaciones son funciones suspend para ejecutarse de forma asíncrona.
 */
interface DentiCoreApi {

    /**
     * Autentica un usuario y retorna el JWT.
     * Endpoint público, no requiere token previo.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /**
     * Obtiene los horarios disponibles para una fecha específica.
     * Endpoint protegido: requiere Authorization Bearer.
     *
     * @param fecha Fecha en formato ISO (ej. 2026-06-25).
     */
    @GET("citas/disponibilidad")
    suspend fun obtenerHorariosDisponibles(
        @Query("fecha") fecha: String
    ): List<HorarioDTO>

    /**
     * Agenda una nueva cita.
     * Endpoint protegido: requiere Authorization Bearer.
     *
     * @param request Datos de la cita a reservar.
     */
    @POST("citas")
    suspend fun agendarCita(@Body request: AgendarCitaRequest)

    /**
     * Obtiene el historial clínico del paciente autenticado.
     * Endpoint protegido: requiere Authorization Bearer.
     */
    @GET("historial/paciente")
    suspend fun obtenerHistorialPaciente(): HistorialDTO
}
