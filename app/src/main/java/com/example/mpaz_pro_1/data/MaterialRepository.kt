
package com.example.mpaz_pro_1.data

class MaterialRepository(
    private val materialDao: MaterialEducativoDao,
    private val asignacionCursoDao: AsignacionCursoDao
) {

    // CREACIÓN DE MATERIAL POR PROFESOR (función anterior)
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

        validarArchivo(nombreArchivo, rutaArchivo)

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

    // NUEVO: UTP CREA Y ASIGNA MATERIAL AL PROFESOR
    suspend fun crearMaterialDesdeUTP(
        rol: String,
        profesorId: Int,
        cursoId: Int,
        asignatura: String,
        unidad: String,
        tema: String,
        descripcion: String,
        nombreArchivo: String,
        rutaArchivo: String
    ): Long {

        require(rol == "UTP") {
            "Solo UTP puede asignar materiales."
        }

        require(profesorId > 0) {
            "Debes seleccionar un profesor válido."
        }

        require(cursoId > 0) {
            "Debes seleccionar un curso válido."
        }

        require(asignatura.isNotBlank()) {
            "Debes ingresar la asignatura."
        }

        require(unidad.isNotBlank()) {
            "Debes ingresar la unidad."
        }

        require(tema.isNotBlank()) {
            "Debes ingresar el tema."
        }

        validarArchivo(nombreArchivo, rutaArchivo)

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

        // Se guarda como BORRADOR.
        // El profesor decidirá cuándo publicarlo.
        return materialDao.insertar(material)
    }

    // VALIDACIÓN DEL PDF
    private fun validarArchivo(
        nombreArchivo: String,
        rutaArchivo: String
    ) {
        require(nombreArchivo.endsWith(".pdf", ignoreCase = true)) {
            "Solo se permiten archivos PDF."
        }

        require(rutaArchivo.isNotBlank()) {
            "Debes seleccionar un archivo PDF."
        }
    }

    // ENVÍO A REVISIÓN
    suspend fun enviarARevision(
        materialId: Int,
        profesorId: Int
    ): Boolean {
        return materialDao.enviarARevision(
            materialId,
            profesorId
        ) > 0
    }

    // CONSULTA DE MATERIALES DEL PROFESOR
    suspend fun obtenerMaterialesProfesor(
        profesorId: Int
    ): List<MaterialEducativo> {
        return materialDao.obtenerDelProfesor(profesorId)
    }

    // CONSULTA DE MATERIALES PENDIENTES PARA UTP
    suspend fun obtenerPendientesUTP(
        rol: String
    ): List<MaterialEducativo> {
        require(rol == "UTP") {
            "Solo UTP puede revisar materiales."
        }

        return materialDao.obtenerPendientesRevision()
    }

    // APROBAR MATERIAL POR UTP
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

    // DEVOLVER MATERIAL POR UTP
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

    // PUBLICACIÓN ANTERIOR POR UTP (se conserva por compatibilidad)
    suspend fun publicarMaterial(
        materialId: Int,
        rol: String
    ): Boolean {
        require(rol == "UTP") {
            "Solo UTP puede publicar mediante este proceso."
        }

        return materialDao.publicar(materialId) > 0
    }

    // NUEVO: EL PROFESOR DECIDE CUÁNDO PUBLICAR
    suspend fun publicarMaterialPorProfesor(
        materialId: Int,
        profesorId: Int
    ): Boolean {
        require(materialId > 0) {
            "Material no válido."
        }

        require(profesorId > 0) {
            "Profesor no válido."
        }

        return materialDao.publicarPorProfesor(
            materialId,
            profesorId
        ) > 0
    }

    // CONSULTA DE MATERIALES PUBLICADOS PARA ESTUDIANTES
    suspend fun obtenerMaterialesPublicados(
        cursoId: Int
    ): List<MaterialEducativo> {
        return materialDao.obtenerPublicadosPorCurso(cursoId)
    }
}
