// Routes.kt
package com.proyectopoo.petcareapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Register

@Serializable
data class RoleSection(
    val userId: Int,
    val apiUserId: String,
    val username: String,
    val email: String
)

@Serializable
object OwnerHome

@Serializable
object OwnerFeed

@Serializable
object CaregiverHome

@Serializable
object CaregiverFeed

@Serializable
object CaregiverService

@Serializable
data class RequestOffer(
    val offeredServiceId: Int,
    val caregiverId: Int
)

@Serializable
data class CreateService(
    val serviceType: String = "",
    val petName: String = ""
)

@Serializable
data class OwnerProfile(
    val ownerId: Int = -1,
    val serviceRequestId: Int = -1
)

@Serializable
data class CaregiverProfile(
    val caregiverId: Int = -1
)

@Serializable
object PasswordRecovery

@Serializable
data class DogInfo(
    val petId: Int = -1
)

@Serializable
data class OwnerPublicProfile(val ownerId: Int)

@Serializable
data class CaregiverPublicProfile(val caregiverId: Int)

@Serializable
data class EditOwnerProfile(val ownerId: Int = -1)

@Serializable
data class EditCaregiverProfile(val caregiverId: Int = -1)

@Serializable
data class Chat(
    val serviceRequestId: Int,
    val currentUserId: Int,
    val otherUserId: Int,
    val otherUserName: String,
    val petId: Int = -1
)

@Serializable
data class ExpedienteMedico(
    val petId: Int,
    val usuarioId: Int
)

@Serializable
data class Historial(
    val usuarioId: Int,
    val role: String
)

@Serializable
data class ResumenServicio(
    val serviceRequestId: Int,
    val requestTitle: String
)

@Serializable
object Filtros

@Serializable
object CompararOfertas

@Serializable
data class Notificaciones(val usuarioId: Int)

@Serializable
object BusquedaGlobal

@Serializable
object Ayuda

@Serializable
data class Configuracion(val usuarioId: Int)

@Serializable
data class EstadisticasCuidador(val caregiverId: Int)

@Serializable
data class EstadisticasPropietario(val ownerId: Int)

@Serializable
data class EditarSolicitud(
    val serviceRequestId: Int
)

@Serializable
data class Favoritos(
    val usuarioId: Int
)

@Serializable
object Onboarding

@Serializable
data class Seguimiento(
    val serviceRequestId: Int
)

@Serializable
data class Calendario(
    val usuarioId: Int
)

@Serializable
data class AlertasPerdidas(
    val usuarioId: Int
)

@Serializable
data class AlertaPerdidaDetalle(
    val alertaId: Int,
    val petsId: Int,
    val reporterId: Int,
    val descripcion: String? = null,
    // Float, no Double: Navigation Compose's automatic NavType inference para rutas
    // type-safe no soporta Double (solo Int/Long/Boolean/Float/String y variantes) -
    // usar Double aqui rompia el arranque de la app con
    // "Cannot cast latitud of type kotlin.Double to a NavType". Float tiene precision
    // mas que suficiente para mostrar un pin en el mapa.
    val latitud: Float,
    val longitud: Float,
    val direccionTexto: String? = null,
    val estado: String,
    val currentUserId: Int
)