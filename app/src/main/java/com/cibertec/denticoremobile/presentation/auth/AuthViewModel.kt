package com.cibertec.denticoremobile.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cibertec.denticoremobile.core.security.TokenManager
import com.cibertec.denticoremobile.data.dto.LoginRequest
import com.cibertec.denticoremobile.data.remote.DentiCoreApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel responsable de orquestar el login del paciente.
 * Recibe [DentiCoreApi] y [TokenManager] por constructor para facilitar testing.
 */
class AuthViewModel(
    private val api: DentiCoreApi,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    /**
     * Ejecuta el login contra el BFF.
     * Emite Loading, Success o Error según corresponda.
     * La llamada de red se ejecuta en [Dispatchers.IO].
     */
    fun login(dni: String, password: String) {
        if (dni.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("El DNI y la contraseña son obligatorios")
            return
        }

        _state.value = AuthState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(dni, password))

                if (response.token.isNotBlank()) {
                    tokenManager.saveToken(response.token)
                    _state.value = AuthState.Success
                } else {
                    _state.value = AuthState.Error("Respuesta inválida del servidor")
                }
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 -> "Credenciales inválidas"
                    403 -> "Acceso denegado"
                    500 -> "Error interno del servidor"
                    else -> "Error de autenticación: ${e.code()}"
                }
                _state.value = AuthState.Error(message)
            } catch (e: IOException) {
                _state.value = AuthState.Error("Sin conexión a internet. Verifique su red.")
            } catch (e: Exception) {
                _state.value = AuthState.Error("Ocurrió un error inesperado: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Reinicia el estado a Idle. Útil cuando la pantalla se vuelve a componer
     * o cuando se desea limpiar un mensaje de error previo.
     */
    fun resetState() {
        _state.value = AuthState.Idle
    }
}

/**
 * Factory manual para construir [AuthViewModel] sin Hilt/Dagger.
 */
@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory(
    private val api: DentiCoreApi,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(api, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
