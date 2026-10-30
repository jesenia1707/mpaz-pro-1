package com.example.mpaz_pro_1.data

class MaterialRepository(
    private val materialDao: MaterialEducativoDao,
    private val asignacionCursoDao: AsignacionCursoDao
) {

    suspend fun crearMaterial(
        profesorId: Int,
        cursoId: Int,
        asignatura: String,
        unidad: String,
        tema: String,
        descripcion: String,
        nombreArchivo: String,
        rutaArchivo: String
    ): Long {
        val cursos = asignacionCursoDao
            .obtenerCursosDelProfesor(profesorId)

        require(cursos.any { it.id == cursoId }) {
            "No tienes asignado este curso."
        }

        require(nombreArchivo.endsWith(".pdf", ignoreCase = true)) {
            "Solo se permiten archivos PDF."
        }

        require(rutaArchivo.isNotBlank()) {
            "Debes seleccionar un archivo PDF."
        }

        val material = MaterialEducativo(
            profesorId = profesorId,
            cursoId = cursoId,
            asignatura = asignatura,
            unidad = unidad,
            tema = tema,
            descripcion = descripcion,
            nombreArchivo = nombreArchivo,
            rutaArchivo = rutaArchivo
        )

        return materialDao.insertar(material)
    }

    suspend fun enviarARevision(
        materialId: Int,
        profesorId: Int
    ): Boolean {
        return materialDao.enviarARevision(
            materialId,
            profesorId
        ) > 0
    }

    suspend fun obtenerMaterialesProfesor(
        profesorId: Int
    ): List<MaterialEducativo> {
        return materialDao.obtenerDelProfesor(profesorId)
    }

    suspend fun obtenerPendientesUTP(
        rol: String
    ): List<MaterialEducativo> {
        require(rol == "UTP") {
            "Solo UTP puede revisar materiales."
        }

        return materialDao.obtenerPendientesRevision()
    }

    suspend fun aprobarMaterial(
        materialId: Int,
        rol: String,
        observacion: String = ""
    ): Boolean {
        require(rol == "UTP") {
            "Solo UTP puede aprobar materiales."
        }

        return materialDao.aprobar(
            materialId,
            observacion
        ) > 0
    }

    suspend fun devolverMaterial(
        materialId: Int,
        rol: String,
        observacion: String
    ): Boolean {
        require(rol == "UTP") {
            "Solo UTP puede devolver materiales."
        }

        require(observacion.isNotBlank()) {
            "Debes indicar el motivo de la devolución."
        }

        return materialDao.devolver(
            materialId,
            observacion
        ) > 0
    }

    suspend fun publicarMaterial(
        materialId: Int,
        rol: String
    ): Boolean {
        require(rol == "UTP") {
            "Solo UTP puede publicar materiales."
        }

        return materialDao.publicar(materialId) > 0
    }

    suspend fun obtenerMaterialesPublicados(
        cursoId: Int
    ): List<MaterialEducativo> {
        return materialDao.obtenerPublicadosPorCurso(cursoId)
    }
}
