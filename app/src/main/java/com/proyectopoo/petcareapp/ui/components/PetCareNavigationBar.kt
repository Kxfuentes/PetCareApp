package com.proyectopoo.petcareapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.proyectopoo.petcareapp.ui.navigation.*
import com.proyectopoo.petcareapp.ui.theme.CafeClaro
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro

@Composable
fun PetCareNavigationBar(
    navController: NavHostController,
    isOwner: Boolean = true
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(containerColor = CafeOscuro) {

        // ── INICIO ──
        val homeRoute = if (isOwner) OwnerHome else CaregiverHome
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute(homeRoute::class) } == true,
            onClick = {
                navController.navigate(homeRoute) {
                    popUpTo(0) { inclusive = true }   // limpiar toda la pila
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = navigationBarColors()
        )

        // ── BUSCAR ──
        val feedRoute = if (isOwner) OwnerFeed else CaregiverFeed
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute(feedRoute::class) } == true,
            onClick = {
                navController.navigate(feedRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = { Text("Buscar") },
            colors = navigationBarColors()
        )

        // ── PUBLICAR (solo dueño) ──
        if (isOwner) {
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.hasRoute<CreateService>() } == true,
                onClick = {
                    navController.navigate(CreateService) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = "Publicar") },
                label = { Text("Publicar") },
                colors = navigationBarColors()
            )
        }

        // ── PERFIL ──
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute<Profile>() } == true,
            onClick = {
                navController.navigate(Profile) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = navigationBarColors()
        )
    }
}

@Composable
private fun navigationBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = Color.White,
    unselectedIconColor = CafeClaro,
    unselectedTextColor = CafeClaro,
    indicatorColor = Color.Transparent
)