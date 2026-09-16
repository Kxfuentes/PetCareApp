package com.proyectopoo.petcareapp.ui.components

import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * true mientras el dispositivo tenga alguna red activa (Parte 3.10). Se actualiza en vivo con
 * [ConnectivityManager.NetworkCallback], mismo mecanismo ya usado en `ChatScreen.kt` para
 * reintentar mensajes pendientes al recuperar conexión.
 */
@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    val isOnline = remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline.value = true
            }

            override fun onLost(network: Network) {
                isOnline.value = connectivityManager?.activeNetwork != null
            }
        }
        runCatching { connectivityManager?.registerDefaultNetworkCallback(callback) }
        isOnline.value = connectivityManager?.activeNetwork != null
        onDispose {
            runCatching { connectivityManager?.unregisterNetworkCallback(callback) }
        }
    }

    return isOnline
}

/** Banner "Sin conexión" que aparece cuando [rememberIsOnline] es false; no ocupa espacio si está online. */
@Composable
fun ConnectivityBanner(modifier: Modifier = Modifier) {
    val isOnline by rememberIsOnline()
    AnimatedVisibility(visible = !isOnline) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = modifier.fillMaxWidth()) {
            Text(
                "🔴 Sin conexión. Los mensajes se enviarán al reconectar.",
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
