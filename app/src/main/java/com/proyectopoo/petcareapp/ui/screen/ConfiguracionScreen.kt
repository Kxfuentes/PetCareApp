package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.BuildConfig
import com.proyectopoo.petcareapp.data.network.NoMolestarRequest
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import com.proyectopoo.petcareapp.data.session.SessionManager
import kotlinx.coroutines.launch

/**
 * Configuración centralizada de la app (Parte 3.9): apariencia (modo oscuro),
 * notificaciones (no molestar, mismo campo de backend que ya usa el perfil de cuidador),
 * privacidad (ver los datos guardados localmente) y acerca de. La app es solo en español,
 * sin selector de idioma.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    usuarioId: Int,
    darkModeOverride: Boolean?,
    onDarkModeOverrideChange: (Boolean?) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var noMolestar by remember { mutableStateOf(false) }
    var showPrivacidad by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SeccionTitulo("Apariencia", Icons.Default.DarkMode)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    listOf(
                        Triple("Seguir el sistema", null as Boolean?, darkModeOverride == null),
                        Triple("Claro", false, darkModeOverride == false),
                        Triple("Oscuro", true, darkModeOverride == true)
                    ).forEach { (label, value, selected) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = { onDarkModeOverrideChange(value) })
                            Text(label)
                        }
                    }
                }
            }

            SeccionTitulo("Notificaciones", Icons.Default.NotificationsOff)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Modo \"no molestar\"", fontWeight = FontWeight.Medium)
                        Text("Silencia las notificaciones push temporalmente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = noMolestar,
                        onCheckedChange = { activo ->
                            noMolestar = activo
                            scope.launch {
                                val response = runCatching {
                                    RetrofitClient.apiService.updateNoMolestar(NoMolestarRequest(usuarioId = usuarioId, activo = activo))
                                }.getOrNull()
                                if (response?.isSuccessful != true) {
                                    noMolestar = !activo
                                    snackbarHostState.showSnackbar("No se pudo actualizar. Intenta de nuevo.")
                                }
                            }
                        }
                    )
                }
            }

            SeccionTitulo("Privacidad", Icons.Default.PrivacyTip)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    TextButton(onClick = { showPrivacidad = !showPrivacidad }) {
                        Text(if (showPrivacidad) "Ocultar mis datos" else "Ver mis datos")
                    }
                    if (showPrivacidad) {
                        Text("Correo: ${sessionManager.getEmail() ?: "—"}")
                        Text("Rol: ${sessionManager.getRole()?.name ?: "—"}")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Para eliminar tu cuenta, contáctanos desde la sección de Ayuda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SeccionTitulo("Acerca de", Icons.Default.Info)
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Text("PetCare v${BuildConfig.VERSION_NAME}")
                    Text(
                        "Plataforma exclusiva para el cuidado de perros. Proyecto académico.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; onLogout() }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SeccionTitulo(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}
