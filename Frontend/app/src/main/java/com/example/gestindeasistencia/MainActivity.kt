package com.example.gestindeasistencia

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.gestindeasistencia.ui.navigation.AppNavGraph
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
            MaterialTheme {

                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    loginViewModel = loginViewModel,
                    asistenciaViewModel = asistenciaViewModel
                )
            }
        }
    }
}
