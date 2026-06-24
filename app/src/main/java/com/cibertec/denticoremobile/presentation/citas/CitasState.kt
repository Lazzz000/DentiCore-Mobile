package com.cibertec.denticoremobile.presentation.citas

import com.cibertec.denticoremobile.data.dto.HorarioDTO

/**
 * Estados reactivos posibles durante el flujo de gestión de citas.
 */
sealed class CitasState {
    data object Idle : CitasState()
    data object Loading : CitasState()
    data class DisponibilidadCargada(val horarios: List<HorarioDTO>) : CitasState()
    data object ReservaExitosa : CitasState()
    data class Error(val message: String) : CitasState()
}
