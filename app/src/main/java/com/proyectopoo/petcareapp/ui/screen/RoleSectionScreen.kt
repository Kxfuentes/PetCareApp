package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.ui.components.PantallaPetCare
import com.proyectopoo.petcareapp.ui.components.TituloPetCare
import com.proyectopoo.petcareapp.ui.components.RolCard

@Composable
fun RoleSectionScreen(
    onRoleSelected: () -> Unit
) {
    PantallaPetCare {
        Spacer(Modifier.height(40.dp))
        TituloPetCare()

        Spacer(Modifier.height(20.dp))

        RolCard("Cuidador", "Quiero ofrecer mis servicios para cuidar mascotas") {
            onRoleSelected()
        }

        Spacer(Modifier.height(16.dp))

        RolCard("Dueño", "Busco a alguien que cuide de mi mejor amigo") {
            onRoleSelected()
        }
    }
}