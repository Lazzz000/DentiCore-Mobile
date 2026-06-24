package com.cibertec.denticoremobile.presentation.historial.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cibertec.denticoremobile.data.dto.DetalleOdontogramaDTO

/**
 * Renderiza una representación gráfica estática del odontograma.
 *
 * **Restricción arquitectónica (EPIC-M03):** Este componente es de solo lectura.
 * No incluye lógica de edición, selección ni eventos onClick en las piezas dentales.
 *
 * @param detalles Lista de detalles del odontograma a visualizar.
 */
@Composable
fun OdontogramaVisual(
    detalles: List<DetalleOdontogramaDTO>
) {
    if (detalles.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay registros de odontograma",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
        return
    }

    // Se ordenan las piezas por su número para una presentación consistente
    val piezasOrdenadas = detalles.sortedBy { it.numeroPieza }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Odontograma",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(piezasOrdenadas, key = { it.numeroPieza }) { pieza ->
                PiezaDentalCelda(
                    numeroPieza = pieza.numeroPieza,
                    estadoTratamiento = pieza.estadoTratamiento
                )
            }
        }
    }
}

/**
 * Celda individual que representa una pieza dental.
 * No es clickeable por requisito de solo lectura.
 */
@Composable
private fun PiezaDentalCelda(
    numeroPieza: Int,
    estadoTratamiento: String
) {
    val colorFondo = colorPorEstadoTratamiento(estadoTratamiento)

    Box(
        modifier = Modifier
            .padding(2.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colorFondo)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = numeroPieza.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Mapea el estado de tratamiento a un color representativo.
 */
@Composable
private fun colorPorEstadoTratamiento(estado: String): Color {
    return when (estado.trim().lowercase()) {
        "pendiente" -> Color(0xFFE53935)          // Rojo
        "en tratamiento", "en_proceso", "encurso" -> Color(0xFFFDD835) // Amarillo
        "completado", "completo", "finalizado" -> Color(0xFF43A047)    // Verde
        "sano", "normal" -> Color(0xFF1E88E5)      // Azul
        else -> MaterialTheme.colorScheme.primary
    }
}
