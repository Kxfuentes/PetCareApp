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
import com.proyectopoo.petcareapp.model.UserRole
import com.proyectopoo.petcareapp.navigation.*
import com.proyectopoo.petcareapp.ui.theme.CafeClaro
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro

@Composable
fun PetCareNavigationBar(
    navController: NavHostController,
    userRole: UserRole?
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = navBackStackEntry?.destination

    val currentRole = userRole ?: UserRole.OWNER

    NavigationBar(containerColor = CafeOscuro) {

        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any {
                it.hasRoute(getHomeRoute(currentRole)::class)
            } == true,
            onClick = { navigateToTopLevel(navController, getHomeRoute(currentRole)) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = navigationBarItemColors()
        )

        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any {
                it.hasRoute(getFeedRoute(currentRole)::class)
            } == true,
            onClick = { navigateToTopLevel(navController, getFeedRoute(currentRole)) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = { Text("Buscar") },
            colors = navigationBarItemColors()
        )

        if (currentRole == UserRole.OWNER) {
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.hasRoute<CreateService>() } == true,
                onClick = { navigateToTopLevel(navController, CreateService()) },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = "Publicar") },
                label = { Text("Publicar") },
                colors = navigationBarItemColors()
            )
        }

        NavigationBarItem(
            selected = currentDestination?.hierarchy?.any {
                it.hasRoute(getProfileRoute(currentRole)::class)
            } == true,
            onClick = { navigateToTopLevel(navController, getProfileRoute(currentRole)) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = navigationBarItemColors()
        )
    }
}


private fun getHomeRoute(role: UserRole): Any = when (role) {
    UserRole.OWNER -> OwnerHome
    UserRole.CAREGIVER -> CaregiverHome
}

private fun getFeedRoute(role: UserRole): Any = when (role) {
    UserRole.OWNER -> OwnerFeed
    UserRole.CAREGIVER -> CaregiverFeed
}

private fun getProfileRoute(role: UserRole): Any = when (role) {
    UserRole.OWNER -> OwnerProfile
    UserRole.CAREGIVER -> CaregiverProfile()
}

private fun navigateToTopLevel(navController: NavHostController, route: Any) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color.White,
    selectedTextColor = Color.White,
    unselectedIconColor = CafeClaro,
    unselectedTextColor = CafeClaro,
    indicatorColor = Color.Transparent
)
