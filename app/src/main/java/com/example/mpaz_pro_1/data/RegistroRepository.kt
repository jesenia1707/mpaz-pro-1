
package com.example.mpaz_pro_1.data

class RegistroRepository(
    private val usuarioDao: UsuarioDao
) {

    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        clave: String,
        rol: String
    ): ResultadoRegistro {

        if (
            nombre.isBlank() ||
            correo.isBlank() ||
            clave.isBlank() ||
            rol.isBlank()
        ) {
            return ResultadoRegistro.Error(
                "Completa todos los campos."
            )
        }

        val correoNormalizado = correo.trim().lowercase()

        if (
            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(correoNormalizado)
                .matches()
        ) {
            return ResultadoRegistro.Error(
                "Ingresa un correo electrónico válido."
            )
        }

        if (clave.length < 8) {
            return ResultadoRegistro.Error(
                "La contraseña debe tener al menos 8 caracteres."
            )
        }

        if (rol !in listOf("Estudiante", "Profesor", "UTP")) {
            return ResultadoRegistro.Error(
                "El perfil seleccionado no es válido."
            )
        }

        if (usuarioDao.buscarPorCorreo(correoNormalizado) != null) {
            return ResultadoRegistro.Error(
                "Ya existe una cuenta con ese correo."
            )
        }

        val usuario = Usuario(
            nombre = nombre.trim(),
            correo = correoNormalizado,
            claveHash = PasswordHasher.hash(clave),
            rol = rol
        )

        return try {
            usuarioDao.insertarUsuario(usuario)
            ResultadoRegistro.Exito
        } catch (_: Exception) {
            ResultadoRegistro.Error(
                "No se pudo guardar la cuenta. Comprueba los datos e inténtalo nuevamente."
            )
        }
    }
}

sealed class ResultadoRegistro {
    data object Exito : ResultadoRegistro()
    data class Error(val mensaje: String) : ResultadoRegistro()
}
