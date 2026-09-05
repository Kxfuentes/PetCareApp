package com.proyectopoo.petcareapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Wrapper reutilizable sobre Coil para cargar imágenes remotas (fotos de
 * perfil, fotos de mascotas, etc.) con caché de memoria/disco automática.
 *
 * Ningún modelo de la app expone hoy una URL de imagen consumida por la UI
 * (no hay campo `fotoPerfilUrl`/`photoUrl` en `User`, `UserEntity`, `PetEntity`,
 * etc.), así que este componente no se usa todavía en ninguna pantalla: queda
 * listo para cuando se agregue ese campo, sin forzar imágenes donde hoy no
 * hay ninguna real que mostrar.
 *
 * Mientras [imageUrl] es nulo/vacío, o si la carga falla, se muestra
 * [placeholderIcon] sobre un círculo con el color de superficie del tema.
 */
@Composable
fun CachedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholderIcon: ImageVector = Icons.Default.Person
) {
    var loadFailed by remember(imageUrl) { mutableStateOf(false) }

    if (imageUrl.isNullOrBlank() || loadFailed) {
        FallbackAvatar(icon = placeholderIcon, modifier = modifier)
        return
    }

    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier.clip(CircleShape),
        contentScale = ContentScale.Crop,
        onError = { loadFailed = true }
    )
}

@Composable
private fun FallbackAvatar(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
