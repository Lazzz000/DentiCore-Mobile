package com.cibertec.denticoremobile.presentation.historial

import com.cibertec.denticoremobile.data.dto.HistorialDTO

/**
 * Estados reactivos posibles durante la carga del historial clínico.
 */
sealed class HistorialState {
    data object Idle : HistorialState()
    data object Loading : HistorialState()
    data class Success(val data: HistorialDTO) : HistorialState()
    data class Error(val message: String) : HistorialState()
}
