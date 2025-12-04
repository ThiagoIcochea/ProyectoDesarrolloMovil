package com.example.gestindeasistencia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.gestindeasistencia.ui.screens.personal.PersonalDetailScreen
import com.example.gestindeasistencia.ui.screens.personal.PersonalFormScreen
import com.example.gestindeasistencia.ui.screens.personal.PersonalListScreen
import com.example.gestindeasistencia.viewmodels.PersonalViewModel
import com.example.gestindeasistencia.ui.screens.report.ReportScreen
import com.example.gestindeasistencia.ui.screens.config.ConfigScreen
import com.example.gestindeasistencia.ui.screens.config.ChangePasswordScreen
import com.example.gestindeasistencia.ui.screens.config.EditProfileScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    asistenciaViewModel: AsistenciaViewModel,
    onDarkThemeChanged: (Boolean) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("reportes") {
            ReportScreen(
                viewModel = asistenciaViewModel,
                onBack = { navController.popBackStack() }
            )
        }
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
                onPerfil = { 
                    val encodedUsername = URLEncoder.encode(username, "UTF-8")
                    val encodedCargo = URLEncoder.encode(cargo, "UTF-8")
                    navController.navigate("editProfile/$encodedUsername/$encodedCargo") 
                },
                onConfig = { 
                    val encodedUsername = URLEncoder.encode(username, "UTF-8")
                    val encodedCargo = URLEncoder.encode(cargo, "UTF-8")
                    navController.navigate("config/$encodedUsername/$encodedCargo") 
                },
                onAsistencia = { navController.navigate("asistencia/$username") },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
        
        // ===== CONFIGURACIÓN =====
        composable(
            route = "config/{username}/{cargo}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("cargo") { type = NavType.StringType }
            )
        ) { backStack ->
            val username = URLDecoder.decode(backStack.arguments?.getString("username") ?: "", "UTF-8")
            val cargo = URLDecoder.decode(backStack.arguments?.getString("cargo") ?: "", "UTF-8")
            
            ConfigScreen(
                userName = username,
                userCargo = cargo,
                onBack = { navController.popBackStack() },
                onEditProfile = { 
                    val encodedUsername = URLEncoder.encode(username, "UTF-8")
                    val encodedCargo = URLEncoder.encode(cargo, "UTF-8")
                    navController.navigate("editProfile/$encodedUsername/$encodedCargo") 
                },
                onChangePassword = { navController.navigate("changePassword") },
                onDarkThemeChanged = onDarkThemeChanged
            )
        }
        
        // ===== EDITAR PERFIL =====
        composable(
            route = "editProfile/{username}/{cargo}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("cargo") { type = NavType.StringType }
            )
        ) { backStack ->
            val username = URLDecoder.decode(backStack.arguments?.getString("username") ?: "", "UTF-8")
            val cargo = URLDecoder.decode(backStack.arguments?.getString("cargo") ?: "", "UTF-8")
            
            EditProfileScreen(
                userName = username,
                userEmail = "$username@empresa.com", // Email por defecto basado en usuario
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        
        // ===== CAMBIAR CONTRASEÑA =====
        composable("changePassword") {
            ChangePasswordScreen(
                onBack = { navController.popBackStack() },
                onPasswordChanged = { navController.popBackStack() }
            )
        }
        
        composable("personal") {
            val vm: PersonalViewModel = viewModel(factory = PersonalViewModel.Factory(LocalContext.current))
            PersonalListScreen(
                viewModel = vm,
                onSelect = { id -> navController.navigate("personalDetalle/$id") },
                onNew = { navController.navigate("personalCrear") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("personalDetalle/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments!!.getInt("id")
            val vm: PersonalViewModel = viewModel(factory = PersonalViewModel.Factory(LocalContext.current))
            PersonalDetailScreen(
                idPersonal = id,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("personalEditar/$id") },
                onDeleted = {
                    navController.navigate("personal") {
                        popUpTo("personal") { inclusive = true }
                    }
                }
            )
        }
        composable("personalCrear") {
            val vm: PersonalViewModel = viewModel(factory = PersonalViewModel.Factory(LocalContext.current))
            PersonalFormScreen(
                idPersonal = null,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate("personal") {
                        popUpTo("personal") { inclusive = true }
                    }
                }
            )
        }
        composable("personalEditar/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { backStack ->
            val id = backStack.arguments!!.getInt("id")
            val vm: PersonalViewModel = viewModel(factory = PersonalViewModel.Factory(LocalContext.current))

            PersonalFormScreen(
                idPersonal = id,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate("personal") {
                        popUpTo("personal") { inclusive = true }
                    }
                }
            )
        }
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
                    loginViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

    }
}
