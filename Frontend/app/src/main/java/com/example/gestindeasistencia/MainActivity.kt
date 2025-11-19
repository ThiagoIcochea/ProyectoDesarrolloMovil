package com.example.gestindeasistencia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestindeasistencia.ui.screens.login.LoginScreen
import com.example.gestindeasistencia.viewmodels.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loginVM = LoginViewModel(this)

        setContent {
            MaterialTheme {
                var logged by remember { mutableStateOf(false) }
                var username by remember { mutableStateOf<String?>(null) }
                var cargo by remember { mutableStateOf<String?>(null) }
                var id by remember { mutableStateOf<Int?>(null) }

                if (!logged) {
                    LoginScreen(viewModel = loginVM) { u, c, i ->
                        username = u
                        cargo = c
                        id = i
                        logged = true
                    }
                } else {
                    HomeScreen(username, cargo, id) {
                        loginVM.logout()
                        logged = false
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(username: String?, cargo: String?, id: Int?, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Bienvenido ${username ?: "Usuario"}")
        Text("Cargo: ${cargo ?: "—"}")
        Text("ID: ${id ?: "—"}")
        Spacer(modifier = Modifier.padding(8.dp))
        androidx.compose.material3.Button(onClick = onLogout) {
            Text("Cerrar sesión")
        }
    }
}