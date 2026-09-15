package com.proyectopoo.petcareapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Application subclass que configura un [ImageLoader] de Coil compartido por toda la
 * app, con caché de memoria y disco explícitas en vez de depender de los valores por
 * defecto de la librería. Coil detecta automáticamente esta clase (vía
 * [ImageLoaderFactory]) y la usa para todas las llamadas a `AsyncImage`/`CachedAsyncImage`,
 * sin necesidad de pasar un `ImageLoader` manualmente en cada composable.
 */
class PetCareApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .build()
    }
}
