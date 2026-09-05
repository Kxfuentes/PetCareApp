package com.proyectopoo.petcareapp.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.proyectopoo.petcareapp.data.session.SessionManager

@Composable
fun OnboardingScreen(
    navController: NavController,
    sessionManager: SessionManager
) {
    val page = remember { mutableStateOf(0) }

    val pages = listOf(
        "Bienvenido a PetCare\nEncuentra cuidadores confiables.",
        "Publica solicitudes y recibe ofertas\nRápido y seguro.",
        "Chatea con cuidadores y gestiona servicios\nTodo en una misma app."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = pages[page.value])
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                if (page.value > 0) page.value = page.value - 1
            }) {
                Text(text = if (page.value == 0) "" else "Atrás")
            }

            if (page.value < pages.lastIndex) {
                Button(onClick = { page.value = page.value + 1 }) {
                    Text(text = "Siguiente")
                }
            } else {
                Button(onClick = {
                    // Mark onboarding as seen and navigate to Login
                    sessionManager.setOnboardingSeen()
                    navController.navigate("Login") {
                        popUpTo(0)
                    }
                }) {
                    Text(text = "Comenzar")
                }
            }
        }
    }
}
