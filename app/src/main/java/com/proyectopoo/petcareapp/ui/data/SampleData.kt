package com.proyectopoo.petcareapp.ui.data

import com.proyectopoo.petcareapp.ui.model.PetService



val listaServicios = listOf(
    PetService(
        1,
        "Max",
        "Carlos",
        "Paseo",
        "Busco paseo matutino",
        "Estelí",
        "1234-5678",
        "08:00 AM"
    ),
    PetService(2, "Luna", "María", "Veterinario", "Cita para vacunas", "Managua", "8888-0000", "02:00 PM"),
    PetService(3, "Rocky", "Pedro", "Baño", "Corte de pelo y baño", "León", "7777-1111", "10:30 AM")
)

data class RoleInfo(val titulo: String, val descripcion: String)

val listaRoles = listOf(
    RoleInfo("Cuidador", "Quiero ofrecer servicios de cuidado"),
    RoleInfo("Dueño", "Busco a alguien que cuide mi mascota")
)