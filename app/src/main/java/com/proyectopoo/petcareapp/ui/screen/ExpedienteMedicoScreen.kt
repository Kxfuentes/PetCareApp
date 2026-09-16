package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.proyectopoo.petcareapp.ui.screen.owner.ExpedienteMedicoSection

/**
 * Vista de solo lectura del expediente médico de una mascota, para que un cuidador pueda
 * consultarlo antes de ofertar a una solicitud, o durante/después de un servicio ya aceptado
 * (reutiliza [ExpedienteMedicoSection] con `isOwner = false`, que ya oculta los botones de
 * agregar/editar/eliminar).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpedienteMedicoScreen(
    petId: Int,
    currentUserId: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                title = {
                    Text("Expediente médico", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ExpedienteMedicoSection(petId = petId, isOwner = false, currentUserId = currentUserId)
        }
    }
}
