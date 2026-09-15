package com.proyectopoo.petcareapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Categorías de emergencia soportadas por el backend (POST /api/emergencias). El valor
 *  enviado a la API es siempre el nombre en mayúsculas; el texto es solo para mostrar. */
val emergencyTypeOptions = listOf(
    "MEDICA" to "Médica",
    "ACCIDENTE" to "Accidente",
    "MASCOTA_PERDIDA" to "Mascota perdida",
    "OTRO" to "Otro"
)

/**
 * Diálogo para reportar una emergencia durante un servicio en curso. Reutilizado tanto en la
 * pantalla del dueño como en la del cuidador -- el llamador decide a quién enviar el reporte
 * (reportedBy) y qué solicitud (service_request_id) armando el request en [onSubmit].
 */
@Composable
fun EmergencyReportDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (tipo: String, descripcion: String?) -> Unit
) {
    var selectedTipo by remember { mutableStateOf(emergencyTypeOptions.first().first) }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Reportar emergencia") },
        text = {
            Column {
                emergencyTypeOptions.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedTipo == value,
                                onClick = { selectedTipo = value }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedTipo == value, onClick = { selectedTipo = value })
                        Text(label)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onSubmit(selectedTipo, descripcion.trim().ifBlank { null }) }
            ) {
                Text(if (isSubmitting) "Enviando..." else "Reportar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Cancelar") }
        }
    )
}
