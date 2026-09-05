package com.proyectopoo.petcareapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.data.local.database.PetCareDatabase
import com.proyectopoo.petcareapp.data.network.FavoritoDto
import com.proyectopoo.petcareapp.data.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * Favoritos del dueño (cuidadores/mascotas marcados). Llama a GET /api/favoritos?usuarioId=
 * y permite quitarlos con DELETE /api/favoritos/{id}.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    usuarioId: Int,
    database: PetCareDatabase,
    onBack: () -> Unit,
    onGoToCaregiverProfile: (Int) -> Unit
) {
    var favoritos by remember { mutableStateOf<List<FavoritoDto>>(emptyList()) }
    var caregiverNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        isLoading = true
        loadError = false
        try {
            val response = RetrofitClient.apiService.getFavoritos(usuarioId)
            if (response.isSuccessful) {
                val list = response.body().orEmpty()
                favoritos = list
                val names = mutableMapOf<Int, String>()
                list.mapNotNull { it.caregiverId }.distinct().forEach { caregiverId ->
                    database.userDao().getUserById(caregiverId)?.let { user ->
                        names[caregiverId] = user.fullName
                    }
                }
                caregiverNames = names
            } else {
                loadError = true
            }
        } catch (e: Exception) {
            loadError = true
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(usuarioId) {
        if (usuarioId > 0) reload() else isLoading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            loadError -> {
                Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No se pudo cargar tus favoritos.", color = MaterialTheme.colorScheme.error)
                }
            }
            favoritos.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Aún no tienes favoritos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoritos, key = { it.id ?: it.hashCode() }) { favorito ->
                        FavoritoCard(
                            favorito = favorito,
                            caregiverName = favorito.caregiverId?.let { caregiverNames[it] },
                            onOpenProfile = { favorito.caregiverId?.let(onGoToCaregiverProfile) },
                            onRemove = {
                                scope.launch {
                                    val id = favorito.id ?: return@launch
                                    runCatching { RetrofitClient.apiService.eliminarFavorito(id) }
                                    reload()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritoCard(
    favorito: FavoritoDto,
    caregiverName: String?,
    onOpenProfile: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                val label = when {
                    favorito.caregiverId != null -> caregiverName ?: "Cuidador #${favorito.caregiverId}"
                    favorito.petId != null -> "Mascota #${favorito.petId}"
                    else -> "Favorito"
                }
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (favorito.caregiverId != null) {
                    TextButton(onClick = onOpenProfile, contentPadding = PaddingValues(0.dp)) {
                        Text("Ver perfil")
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar de favoritos")
            }
        }
    }
}
