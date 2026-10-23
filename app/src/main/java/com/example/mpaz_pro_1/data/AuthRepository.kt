
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
            return ResultadoLogin.Error(
                "Completa tu usuario y contraseña."
            )
        }

        val correoNormalizado = correo.trim().lowercase()
        val usuario = usuarioDao.buscarPorCorreo(correoNormalizado)

        if (usuario == null) {
            return ResultadoLogin.Error(
                "DIAGNÓSTICO: no se encontró el correo en Room."
            )
        }

        if (!PasswordHasher.verify(clave, usuario.claveHash)) {
            return ResultadoLogin.Error(
                "DIAGNÓSTICO: el correo existe, pero la contraseña no coincide."
            )
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
