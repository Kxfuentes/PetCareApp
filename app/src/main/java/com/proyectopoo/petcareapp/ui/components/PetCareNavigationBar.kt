package com.proyectopoo.petcareapp.ui.components

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.proyectopoo.petcareapp.ui.navigation.* import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.FondoCampo

@SuppressLint("RestrictedApi")
@Composable
fun PetCareNavigationBar(navController: NavHostController) {
    NavigationBar(
        containerColor = FondoCampo,
        contentColor = CafeMedio
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentDestination = navBackStackEntry?.destination

        // Ítem INICIO
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute<Feed>() } == true,
            onClick = {
                navController.navigate(Feed) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )

        // Ítem PUBLICAR
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute<CreateService>() } == true,
            onClick = {
                navController.navigate(CreateService) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Add, contentDescription = "Publicar") },
            label = { Text("Publicar") }
        )

        // Ítem PERFIL
        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any { it.hasRoute<Profile>() } == true,
            onClick = {
                navController.navigate(Profile) {
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}