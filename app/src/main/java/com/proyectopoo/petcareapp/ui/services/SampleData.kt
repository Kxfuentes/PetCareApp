package com.proyectopoo.petcareapp.ui.services
/**
 * Lista de servicios de ejemplo.
 * Esto simula datos reales para poder visualizar el Feed.
 */
val listaServicios = mutableListOf(

    PetService(
        id = 1,
        nombreMascota = "Milo",
        nombreDueno = "David Ramírez",
        tipoServicio = "Veterinario",
        descripcion = "Necesito llevar a Milo al veterinario para revisión general.",
        ubicacion = "Managua",
        contacto = "8888-8888",
        hora = "10:00 AM"
    ),

    PetService(
        id = 2,
        nombreMascota = "Luna",
        nombreDueno = "María López",
        tipoServicio = "Paseo",
        descripcion = "Busco alguien que pasee a Luna por 1 hora.",
        ubicacion = "Masaya",
        contacto = "7777-7777",
        hora = "3:00 PM"
    ),

    PetService(
        id = 3,
        nombreMascota = "Rocky",
        nombreDueno = "Carlos Pérez",
        tipoServicio = "Cuidado",
        descripcion = "Necesito que cuiden a Rocky durante el día.",
        ubicacion = "Granada",
        contacto = "6666-6666",
        hora = "Todo el día"
    )
)