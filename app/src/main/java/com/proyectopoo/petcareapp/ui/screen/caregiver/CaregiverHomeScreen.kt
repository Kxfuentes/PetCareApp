package com.proyectopoo.petcareapp.ui.screen.caregiver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.*

@Composable
fun CaregiverHomeScreen(
    onGoToFeed: () -> Unit,
    onGoToCreate: () -> Unit,
    onGoToServices: () -> Unit,
    onGoToProfile: () -> Unit
) {
    var available by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(scrollState)
    ) {
        // Header Moderno
        Surface(
            color = CafeOscuro,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                Text(
                    text = "Panel de Cuidador",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gestiona tus servicios y solicitudes",
                    color = CafeClaro,
                    fontSize = 16.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Estado de Disponibilidad
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(22.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estado actual", color = TextoSuave, fontSize = 14.sp)
                        Text(
                            text = if (available) "Disponible para trabajar" else "En descanso",
                            color = CafeOscuro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Switch(
                        checked = available,
                        onCheckedChange = { available = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CafeMedio
                        )
                    )
                }
            }

            // Próximo Servicio
            Text(text = "Próximo compromiso", color = CafeOscuro, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(22.dp)),
                border = BorderStroke(1.dp, CafeClaro.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FondoCrema,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(Icons.Default.DirectionsWalk, null, tint = CafeMedio, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Paseo con Max", color = CafeOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, tint = TextoSuave, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(text = "Hoy, 3:00 PM", color = TextoSuave, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Gestión de Servicios
            Text(text = "Tu actividad", color = CafeOscuro, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            // Grid de acciones
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = "Mis Servicios",
                    icon = Icons.Default.List,
                    onClick = onGoToServices,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Solicitudes",
                    icon = Icons.Default.Search,
                    onClick = onGoToFeed,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(
                    title = "Publicar",
                    icon = Icons.Default.AddCircle,
                    onClick = onGoToCreate,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Mi Perfil",
                    icon = Icons.Default.Person,
                    onClick = onGoToProfile,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CafeClaro.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = CafeMedio, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = CafeOscuro, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
