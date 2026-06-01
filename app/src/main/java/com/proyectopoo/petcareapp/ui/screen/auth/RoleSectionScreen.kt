package com.proyectopoo.petcareapp.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.data.network.RegisterRequest
import com.proyectopoo.petcareapp.ui.theme.TextoSuave

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSectionScreen(
    userId: Int,
    username: String,
    email: String,
    password: String,
    onOwnerSelected: () -> Unit,
    onCaregiverSelected: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    Scaffold{ paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Cómo usarás PetCare?",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Esta decisión define tu experiencia en la plataforma",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))


            RoleCard(
                title = "Soy dueño de perrito",
                description = "Busco de personas que me ayuden a birndarle el mejor cuidado a mi mascota",
                icon = Icons.Outlined.Pets,
                isSelected = selectedRole == "OWNER",
                onClick = { selectedRole = "OWNER" }
            )

            Spacer(modifier = Modifier.height(20.dp))

            RoleCard(
                title = "Quiero ser cuidador",
                description = "Ofrezco mis servicios para ayudar en el cuidado de los perritos",
                icon = Icons.Outlined.Favorite,
                isSelected = selectedRole == "CAREGIVER",
                onClick = { selectedRole = "CAREGIVER" }
            )

            Spacer(modifier = Modifier.weight(1f))


            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Esta elección es permanente y define tu rol principal en la plataforma.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (selectedRole != null) {
                        if (selectedRole == "OWNER") onOwnerSelected()
                        else onCaregiverSelected()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedRole != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "Continuar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
