package com.example.gestindeasistencia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gestindeasistencia.ui.screens.dashboard.DashboardScreen
import com.example.gestindeasistencia.ui.screens.empleado.EmpleadoAsistenciaScreen
import com.example.gestindeasistencia.ui.screens.login.LoginScreen
import com.example.gestindeasistencia.viewmodels.AsistenciaViewModel
import com.example.gestindeasistencia.viewmodels.LoginViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun AppNavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    asistenciaViewModel: AsistenciaViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // ---------------- LOGIN ----------------
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { username, cargo, id ->

                    navController.navigate("dashboard/$username/$cargo/$id") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // ---------------- DASHBOARD ----------------
        composable(
            route = "dashboard/{username}/{cargo}/{id}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("cargo") { type = NavType.StringType },
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStack ->

            val username = backStack.arguments?.getString("username") ?: ""
            val cargo = backStack.arguments?.getString("cargo") ?: ""
            val id = backStack.arguments?.getInt("id") ?: 0

            DashboardScreen(
                userName = username,
                userCargo = cargo,

                onGestionPersonal = { navController.navigate("personal") },
                onReportes = { navController.navigate("reportes") },
                onPerfil = { navController.navigate("perfil") },
                onConfig = { navController.navigate("config") },
                onAsistencia = { navController.navigate("asistencia/$username") },

                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        // ---------------- ASISTENCIA ----------------
        composable(
            route = "asistencia/{username}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStack ->
            val username = backStack.arguments?.getString("username") ?: ""
            
            EmpleadoAsistenciaScreen(
                viewModel = asistenciaViewModel,
                username = username,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    // Al hacer logout desde asistencia, volver al login
                    loginViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
