
package com.example.mpaz_pro_1.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val ESTUDIANTE = "estudiante"
    const val PROFESOR = "profesor"
    const val UTP = "utp"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    pantallaLogin: @Composable () -> Unit,
    pantallaRegistro: @Composable () -> Unit,
    pantallaEstudiante: @Composable () -> Unit,
    pantallaProfesor: @Composable () -> Unit,
    pantallaUTP: @Composable () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        composable(Rutas.LOGIN) {
            pantallaLogin()
        }

        composable(Rutas.REGISTRO) {
            pantallaRegistro()
        }

        composable(Rutas.ESTUDIANTE) {
            pantallaEstudiante()
        }

        composable(Rutas.PROFESOR) {
            pantallaProfesor()
        }

        composable(Rutas.UTP) {
            pantallaUTP()
        }
    }
}
