package com.proyectopoo.petcareapp.ui.components

/*
 * Comentario de modulo PetCare:
 * Componente reutilizable de interfaz. Se mantiene aislado para repetirlo sin duplicar codigo.
 */

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserBasicInfoCard(
    name: String,
    email: String,
    role: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleLarge)
            Text(text = email, style = MaterialTheme.typography.bodyMedium)
            Text(text = role, style = MaterialTheme.typography.bodyMedium)
        }
    }
}