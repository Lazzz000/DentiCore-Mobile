package com.cibertec.denticoremobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.cibertec.denticoremobile.core.navigation.AppNavigation
import com.cibertec.denticoremobile.ui.theme.DentiCoreMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DentiCoreMobileTheme {
                RequestNotificationPermission()
                AppNavigation()
            }
        }
    }
}

/**
 * Solicita el permiso POST_NOTIFICATIONS en Android 13+ (API 33).
 * Si el usuario lo deniega, la app continúa ejecutándose normalmente
 * porque las notificaciones son un complemento, no un bloqueador del flujo clínico.
 */
@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = androidx.compose.ui.platform.LocalContext.current
    val permission = Manifest.permission.POST_NOTIFICATIONS

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // No se bloquea la app si el permiso es denegado.
        // El servicio FCM seguirá recibiendo mensajes, pero no mostrará notificaciones visuales.
        if (!isGranted) {
            android.util.Log.w(
                "DentiCorePermissions",
                "Permiso de notificaciones denegado. Las alertas visuales están deshabilitadas."
            )
        }
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            permissionLauncher.launch(permission)
        }
    }
}
