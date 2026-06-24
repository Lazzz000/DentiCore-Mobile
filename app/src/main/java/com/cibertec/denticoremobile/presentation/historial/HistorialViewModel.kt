package com.cibertec.denticoremobile.presentation.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cibertec.denticoremobile.data.remote.DentiCoreApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel responsable de cargar el historial clínico del paciente autenticado.
 * Operación exclusivamente de lectura según EPIC-M03.
 */
class HistorialViewModel(
    private val api: DentiCoreApi
) : ViewModel() {

    private val _state = MutableStateFlow<HistorialState>(HistorialState.Idle)
    val state: StateFlow<HistorialState> = _state.asStateFlow()

    /**
     * Consume el endpoint GET /historial/paciente.
     * La llamada de red se ejecuta en [Dispatchers.IO].
     */
    fun cargarHistorial() {
        _state.value = HistorialState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val historial = api.obtenerHistorialPaciente()
                _state.value = HistorialState.Success(historial)
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 -> "Sesión expirada. Vuelva a iniciar sesión."
                    403 -> "No tiene permisos para ver esta información."
                    404 -> "No se encontró historial clínico."
                    500 -> "Error interno del servidor."
                    else -> "Error al cargar el historial: ${e.code()}"
                }
                _state.value = HistorialState.Error(message)
            } catch (e: IOException) {
                _state.value = HistorialState.Error("Sin conexión a internet. Verifique su red.")
            } catch (e: Exception) {
                _state.value = HistorialState.Error("Ocurrió un error inesperado: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Reinicia el estado a Idle.
     */
    fun resetState() {
        _state.value = HistorialState.Idle
    }
}

/**
 * Factory manual para construir [HistorialViewModel] sin Hilt/Dagger.
 */
@Suppress("UNCHECKED_CAST")
class HistorialViewModelFactory(
    private val api: DentiCoreApi
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistorialViewModel::class.java)) {
            return HistorialViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
