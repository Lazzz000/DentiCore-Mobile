package com.cibertec.denticoremobile.presentation.citas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cibertec.denticoremobile.data.dto.HorarioDTO
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Pantalla principal de gestión de citas.
 *
 * @param viewModel Instancia de [CitasViewModel] construida con su factory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(
    viewModel: CitasViewModel
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var horarioSeleccionado by remember { mutableStateOf<HorarioDTO?>(null) }

    // Carga inicial y recarga cuando cambia la fecha
    LaunchedEffect(fechaSeleccionada) {
        viewModel.cargarDisponibilidad(fechaSeleccionada)
    }

    // Escucha reservas exitosas para mostrar snackbar y recargar disponibilidad
    LaunchedEffect(state) {
        if (state is CitasState.ReservaExitosa) {
            snackbarHostState.showSnackbar("Cita agendada exitosamente")
            viewModel.cargarDisponibilidad(fechaSeleccionada)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Agendar Nueva Cita",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SelectorFecha(
                fechaSeleccionada = fechaSeleccionada,
                onMostrarDatePicker = { mostrarDatePicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Horarios disponibles",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (state) {
                is CitasState.Loading -> {
                    BoxLoading()
                }

                is CitasState.Error -> {
                    Text(
                        text = (state as CitasState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is CitasState.DisponibilidadCargada -> {
                    val horarios = (state as CitasState.DisponibilidadCargada).horarios
                    ListaHorarios(
                        horarios = horarios,
                        onHorarioSeleccionado = { horarioSeleccionado = it }
                    )
                }

                else -> {
                    // Idle / ReservaExitosa: no se renderiza contenido adicional
                }
            }
        }
    }

    if (mostrarDatePicker) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let {
                    fechaSeleccionada = millisToIsoDate(it)
                }
                mostrarDatePicker = false
            },
            onDismiss = { mostrarDatePicker = false }
        )
    }

    horarioSeleccionado?.let { horario ->
        ConfirmacionReservaDialog(
            horario = horario,
            onConfirmar = {
                viewModel.agendarCita(
                    idMedico = horario.idOdontologo,
                    fecha = fechaSeleccionada,
                    hora = horario.fechaHora.substringAfter("T", "00:00").take(5)
                )
                horarioSeleccionado = null
            },
            onCancelar = { horarioSeleccionado = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(
    fechaSeleccionada: String,
    onMostrarDatePicker: () -> Unit
) {
    OutlinedTextField(
        value = fechaSeleccionada,
        onValueChange = { },
        label = { Text("Fecha") },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            TextButton(onClick = onMostrarDatePicker) {
                Text("Cambiar")
            }
        }
    )
}

@Composable
private fun ListaHorarios(
    horarios: List<HorarioDTO>,
    onHorarioSeleccionado: (HorarioDTO) -> Unit
) {
    if (horarios.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay horarios disponibles para esta fecha",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        return
    }

    LazyColumn {
        items(horarios, key = { it.id }) { horario ->
            HorarioItem(
                horario = horario,
                onClick = { onHorarioSeleccionado(horario) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HorarioItem(
    horario: HorarioDTO,
    onClick: () -> Unit
) {
    val horaFormateada = remember(horario.fechaHora) {
        horario.fechaHora.substringAfter("T", "--:--").take(5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = horario.disponible,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (horario.disponible) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = horaFormateada,
                style = MaterialTheme.typography.titleMedium,
                color = if (horario.disponible) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
            Text(
                text = "${horario.nombreOdontologo} · ${horario.especialidad}",
                style = MaterialTheme.typography.bodySmall,
                color = if (horario.disponible) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
            if (!horario.disponible) {
                Text(
                    text = "No disponible",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ConfirmacionReservaDialog(
    horario: HorarioDTO,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val hora = horario.fechaHora.substringAfter("T", "--:--").take(5)

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Confirmar reserva") },
        text = {
            Text(
                "¿Desea agendar una cita con ${horario.nombreOdontologo} " +
                        "a las $hora?"
            )
        },
        confirmButton = {
            Button(onClick = onConfirmar) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun BoxLoading() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Cargando horarios...", style = MaterialTheme.typography.bodyMedium)
    }
}

/**
 * Convierte millis de epoch a fecha ISO (yyyy-MM-dd) usando la zona horaria del dispositivo.
 */
private fun millisToIsoDate(millis: Long): String {
    return Instant
        .ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ISO_LOCAL_DATE)
}
