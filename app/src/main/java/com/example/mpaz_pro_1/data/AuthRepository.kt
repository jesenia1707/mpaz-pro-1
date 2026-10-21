
package com.example.mpaz_pro_1.data

class AuthRepository(
    private val usuarioDao: UsuarioDao
) {

    suspend fun iniciarSesion(
        correo: String,
        clave: String,
        rolSeleccionado: String
    ): ResultadoLogin {
        if (correo.isBlank() || clave.isBlank()) {
            return ResultadoLogin.Error("Completa tu usuario y contraseña.")
        }

        val usuario = usuarioDao.buscarPorCorreo(correo.trim())

        if (usuario == null ||
            !PasswordHasher.verify(clave, usuario.claveHash)
        ) {
            return ResultadoLogin.Error("Correo o contraseña incorrectos.")
        }

        if (!usuario.rol.equals(rolSeleccionado, ignoreCase = true)) {
            return ResultadoLogin.Error(
                "El perfil seleccionado no corresponde a esta cuenta."
            )
        }

        return ResultadoLogin.Exito(usuario)
    }
}

sealed class ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
}
