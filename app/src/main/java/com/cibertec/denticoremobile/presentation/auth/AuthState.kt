package com.cibertec.denticoremobile.presentation.auth

/**
 * Estados reactivos posibles durante el flujo de autenticación.
 */
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
