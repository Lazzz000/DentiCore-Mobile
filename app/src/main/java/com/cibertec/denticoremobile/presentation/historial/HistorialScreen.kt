package com.cibertec.denticoremobile.presentation.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cibertec.denticoremobile.data.dto.AtencionDTO
import com.cibertec.denticoremobile.presentation.historial.components.OdontogramaVisual

/**
 * Pantalla principal del módulo "Mi Historial".
 *
 * @param viewModel Instancia de [HistorialViewModel] construida con su factory.
 */
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarHistorial()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mi Historial Clínico",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (state) {
            is HistorialState.Loading -> {
                BoxLoading()
            }

            is HistorialState.Error -> {
                Text(
                    text = (state as HistorialState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            is HistorialState.Success -> {
                val historial = (state as HistorialState.Success).data
                HistorialContent(historial)
            }

            else -> {
                // Estado Idle: no se renderiza nada hasta que LaunchedEffect dispare la carga
            }
        }
    }
}

@Composable
private fun BoxLoading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Cargando historial...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HistorialContent(historial: com.cibertec.denticoremobile.data.dto.HistorialDTO) {
    // Se toma el odontograma de la atención más reciente (primera de la lista asumiendo orden descendente)
    val detallesOdontograma = historial.atenciones
        .firstOrNull()
        ?.odontograma
        ?.detalles
        ?: emptyList()

    // Mitad superior: odontograma visual
    OdontogramaVisual(detalles = detallesOdontograma)

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Atenciones previas",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    // Mitad inferior: listado de atenciones clínicas
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(historial.atenciones) { atencion ->
            AtencionItem(atencion = atencion)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AtencionItem(atencion: AtencionDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Fecha: ${atencion.fechaAtencion}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Motivo: ${atencion.motivoConsulta}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!atencion.notasClinicas.isNullOrBlank()) {
                Text(
                    text = "Notas: ${atencion.notasClinicas}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
