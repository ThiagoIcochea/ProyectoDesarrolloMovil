package com.example.gestindeasistencia

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.gestindeasistencia.ui.navigation.AppNavGraph
import com.example.gestindeasistencia.ui.theme.GestiónDeAsistenciaTheme
import com.example.gestindeasistencia.utils.SettingsPrefs
import com.example.gestindeasistencia.viewmodels.AsistenciaViewModel
import com.example.gestindeasistencia.viewmodels.LoginViewModel

class MainActivity : FragmentActivity() {

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear AsistenciaViewModel
        val asistenciaViewModel = AsistenciaViewModel(this)

        setContent {
            // Estado del tema oscuro
            val systemDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { 
                mutableStateOf(SettingsPrefs.isDarkTheme(applicationContext)) 
            }
            
            GestiónDeAsistenciaTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false // Desactivar color dinámico para mantener consistencia
            ) {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    asistenciaViewModel = asistenciaViewModel,
                    onDarkThemeChanged = { enabled ->
                        isDarkTheme = enabled
                    }
                )
            }
        }
    }
}
