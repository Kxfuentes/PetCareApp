package com.proyectopoo.petcareapp.ui.components

/*
 * Comentario de modulo PetCare:
 * Componente reutilizable de interfaz. Se mantiene aislado para repetirlo sin duplicar codigo.
 */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.NominatimClient
import com.proyectopoo.petcareapp.model.NominatimResponse
import kotlinx.coroutines.delay

/**
 * Campo de texto con autocompletado de direcciones usando la API de Nominatim.
 *
 * El padre es dueño del texto ([query]) y de las coordenadas (vía [onLocationPicked]).
 * Mientras el usuario escribe se buscan sugerencias (con debounce); al tocar una,
 * se rellena el texto con la dirección completa y se entregan sus coordenadas.
 */
@Composable
fun LocationAutocompleteField(
    query: String,
    onQueryChange: (String) -> Unit,
    onLocationPicked: (NominatimResponse) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Ubicación",
    isError: Boolean = false
) {
    var results by remember { mutableStateOf<List<NominatimResponse>>(emptyList()) }
    var showResults by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    // Evita relanzar la búsqueda justo después de seleccionar una sugerencia.
    var suppressSearch by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (suppressSearch) {
            suppressSearch = false
            return@LaunchedEffect
        }
        val q = query.trim()
        if (q.length < 3) {
            results = emptyList()
            showResults = false
            return@LaunchedEffect
        }
        delay(450) // debounce: se cancela si el texto cambia antes de cumplirse
        isSearching = true
        try {
            val response = NominatimClient.instance.searchLocation(query = q, limit = 5)
            results = response
            showResults = response.isNotEmpty()
        } catch (e: Exception) {
            results = emptyList()
            showResults = false
        } finally {
            isSearching = false
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            singleLine = true,
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Place, contentDescription = null)
                }
            }
        )

        if (showResults) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    results.forEach { result ->
                        Text(
                            text = result.display_name,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    suppressSearch = true
                                    onLocationPicked(result)
                                    showResults = false
                                    results = emptyList()
                                }
                                .padding(12.dp)
                        )
                        HorizontalDivider(color = Color.LightGray)
                    }
                }
            }
        }
    }
}
