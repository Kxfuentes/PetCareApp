package com.proyectopoo.petcareapp.data

import com.proyectopoo.petcareapp.model.PetService



val listaServicios = listOf(
    PetService(
        "Max",
        "Carlos",
        "Paseo",
        "Busco paseo matutino",
        "Estelí",
        "1234-5678",
        "08:00 AM"
    ),
    PetService( "Luna", "María", "Veterinario", "Cita para vacunas", "Managua", "8888-0000", "02:00 PM"),
    PetService( "Rocky", "Pedro", "Baño", "Corte de pelo y baño", "León", "7777-1111", "10:30 AM")
)

data class RoleInfo(val titulo: String, val descripcion: String)

val listaRoles = listOf(
    RoleInfo("Cuidador", "Quiero ofrecer servicios de cuidado"),
    RoleInfo("Dueño", "Busco a alguien que cuide mi mascota")
)