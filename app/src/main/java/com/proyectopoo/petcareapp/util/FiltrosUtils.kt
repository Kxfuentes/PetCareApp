package com.proyectopoo.petcareapp.util

import com.proyectopoo.petcareapp.ui.screen.FiltrosResult

/**
 * Distancia en km entre el usuario y un ítem, o null si falta alguna coordenada
 * (usuario sin ubicación guardada, u oferta/solicitud sin lat/lng).
 */
fun distanceKmOrNull(
    userLat: Double?, userLon: Double?,
    itemLat: Double?, itemLon: Double?
): Double? {
    if (userLat == null || userLon == null || itemLat == null || itemLon == null) return null
    return DistanceUtils.haversineKm(userLat, userLon, itemLat, itemLon)
}

/**
 * Evalúa si un ítem del feed cumple los filtros aplicados. Si algún dato necesario para
 * evaluar un filtro no está disponible (rating o distancia null), ese filtro no excluye el
 * ítem: se prefiere mostrar de más a ocultar resultados por datos faltantes.
 */
fun matchesFiltros(
    filters: FiltrosResult?,
    serviceTypeName: String?,
    rating: Double?,
    distanceKm: Double?
): Boolean {
    if (filters == null) return true
    if (filters.serviceType != null && !serviceTypeName.equals(filters.serviceType, ignoreCase = true)) return false
    if (rating != null && rating < filters.minRating) return false
    // distanceKm == null means we couldn't compute it (missing coordinates) — don't exclude, fail open.
    if (distanceKm != null && distanceKm > filters.radiusKm) return false
    return true
}
