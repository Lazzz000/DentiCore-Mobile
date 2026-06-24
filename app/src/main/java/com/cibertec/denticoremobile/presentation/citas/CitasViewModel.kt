package com.cibertec.denticoremobile.presentation.citas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cibertec.denticoremobile.data.dto.AgendarCitaRequest
import com.cibertec.denticoremobile.data.remote.DentiCoreApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel responsable de gestionar la disponibilidad y reserva de citas.
 *
 * Para agilizar el MVP se asume un idPaciente e idMedico por defecto.
 * En una versión productiva estos valores deben provenir del perfil autenticado.
 */
class CitasViewModel(
    private val api: DentiCoreApi
) : ViewModel() {

    private val _state = MutableStateFlow<CitasState>(CitasState.Idle)
    val state: StateFlow<CitasState> = _state.asStateFlow()

    /**
     * Carga la disponibilidad de horarios para una fecha dada.
     *
     * @param fecha Fecha en formato ISO (ej. 2026-06-25).
     */
    fun cargarDisponibilidad(fecha: String) {
        _state.value = CitasState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val horarios = api.obtenerHorariosDisponibles(fecha)
                _state.value = CitasState.DisponibilidadCargada(horarios)
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 -> "Sesión expirada. Vuelva a iniciar sesión."
                    500 -> "Error interno del servidor."
                    else -> "Error al cargar disponibilidad: ${e.code()}"
                }
                _state.value = CitasState.Error(message)
            } catch (e: IOException) {
                _state.value = CitasState.Error("Sin conexión a internet. Verifique su red.")
            } catch (e: Exception) {
                _state.value = CitasState.Error("Ocurrió un error inesperado: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Intenta reservar una cita con el médico y fecha/hora seleccionados.
     * Ante un HTTP 409 (conflicto de concurrencia) notifica al usuario y
     * recarga la disponibilidad para reflejar el estado actual del backend.
     *
     * @param idMedico Identificador del odontólogo (por defecto 1 para MVP).
     * @param fecha Fecha seleccionada (ej. 2026-06-25).
     * @param hora Hora seleccionada (ej. 15:30).
     */
    fun agendarCita(
        idMedico: Int = DEFAULT_MEDICO_ID,
        fecha: String,
        hora: String
    ) {
        _state.value = CitasState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fechaHoraIso = construirFechaHoraIso(fecha, hora)
                val request = AgendarCitaRequest(
                    idPaciente = DEFAULT_PACIENTE_ID,
                    idOdontologo = idMedico,
                    fechaHora = fechaHoraIso
                )

                api.agendarCita(request)
                _state.value = CitasState.ReservaExitosa
            } catch (e: HttpException) {
                when (e.code()) {
                    409 -> {
                        _state.value = CitasState.Error("Turno ya no disponible")
                        cargarDisponibilidad(fecha)
                    }

                    400 -> _state.value = CitasState.Error("Datos de reserva inválidos.")
                    401 -> _state.value = CitasState.Error("Sesión expirada. Vuelva a iniciar sesión.")
                    500 -> _state.value = CitasState.Error("Error interno del servidor.")
                    else -> _state.value = CitasState.Error("Error al reservar: ${e.code()}")
                }
            } catch (e: IOException) {
                _state.value = CitasState.Error("Sin conexión a internet. Verifique su red.")
            } catch (e: Exception) {
                _state.value = CitasState.Error("Ocurrió un error inesperado: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Reinicia el estado a Idle.
     */
    fun resetState() {
        _state.value = CitasState.Idle
    }

    /**
     * Construye una fecha/hora en formato ISO 8601 a partir de fecha y hora locales.
     */
    private fun construirFechaHoraIso(fecha: String, hora: String): String {
        return try {
            val localDate = LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE)
            val localTime = LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"))
            localDate.atTime(localTime).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            // Fallback simple si el parseo falla
            "${fecha}T${hora}:00"
        }
    }

    companion object {
        private const val DEFAULT_PACIENTE_ID = 1
        private const val DEFAULT_MEDICO_ID = 1
    }
}

/**
 * Factory manual para construir [CitasViewModel] sin Hilt/Dagger.
 */
@Suppress("UNCHECKED_CAST")
class CitasViewModelFactory(
    private val api: DentiCoreApi
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CitasViewModel::class.java)) {
            return CitasViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
