package com.cibertec.denticoremobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cibertec.denticoremobile.core.network.RetrofitClient
import com.cibertec.denticoremobile.core.security.TokenManager
import com.cibertec.denticoremobile.presentation.auth.AuthViewModel
import com.cibertec.denticoremobile.presentation.auth.AuthViewModelFactory
import com.cibertec.denticoremobile.presentation.auth.LoginScreen
import com.cibertec.denticoremobile.presentation.citas.CitasScreen
import com.cibertec.denticoremobile.presentation.citas.CitasViewModelFactory
import com.cibertec.denticoremobile.presentation.historial.HistorialScreen
import com.cibertec.denticoremobile.presentation.historial.HistorialViewModelFactory
import com.cibertec.denticoremobile.presentation.home.HomeScreen

/**
 * Rutas principales de la aplicación.
 */
object AppRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val HISTORIAL = "historial"
    const val CITAS = "citas"
}

/**
 * Grafo de navegación base de la aplicación.
 *
 * @param navHostController Controlador de navegación. Si no se proporciona, se crea uno nuevo.
 * @param startDestination Ruta inicial del grafo.
 */
@Composable
fun AppNavigation(
    navHostController: NavHostController = rememberNavController(),
    startDestination: String = AppRoutes.LOGIN
) {
    val context = LocalContext.current
    val api = RetrofitClient.getApi(context)
    val tokenManager = TokenManager(context)

    NavHost(
        navController = navHostController,
        startDestination = startDestination
    ) {
        composable(route = AppRoutes.LOGIN) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(api, tokenManager)
            )

            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navHostController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppRoutes.HOME) {
            HomeScreen(
                onVerHistorial = {
                    navHostController.navigate(AppRoutes.HISTORIAL)
                },
                onNavigateToCitas = {
                    navHostController.navigate(AppRoutes.CITAS)
                }
            )
        }

        composable(route = AppRoutes.HISTORIAL) {
            val historialViewModel: com.cibertec.denticoremobile.presentation.historial.HistorialViewModel = viewModel(
                factory = HistorialViewModelFactory(api)
            )

            HistorialScreen(viewModel = historialViewModel)
        }

        composable(route = AppRoutes.CITAS) {
            val citasViewModel: com.cibertec.denticoremobile.presentation.citas.CitasViewModel = viewModel(
                factory = CitasViewModelFactory(api)
            )

            CitasScreen(viewModel = citasViewModel)
        }
    }
}
