package com.example.mpaz_pro_1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MaterialEducativoDao {

    @Insert
    suspend fun insertar(material: MaterialEducativo): Long

    @Update
    suspend fun actualizar(material: MaterialEducativo)

    @Query("""
        SELECT * FROM materiales_educativos
        WHERE profesorId = :profesorId
        ORDER BY fechaCreacion DESC
    """)
    suspend fun obtenerDelProfesor(
        profesorId: Int
    ): List<MaterialEducativo>

    @Query("""
        SELECT * FROM materiales_educativos
        WHERE estado = 'EN_REVISION'
        ORDER BY fechaCreacion ASC
    """)
    suspend fun obtenerPendientesRevision(): List<MaterialEducativo>

    @Query("""
        SELECT * FROM materiales_educativos
        WHERE cursoId = :cursoId
          AND estado = 'PUBLICADO'
        ORDER BY asignatura, unidad, tema
    """)
    suspend fun obtenerPublicadosPorCurso(
        cursoId: Int
    ): List<MaterialEducativo>

    @Query("""
        SELECT * FROM materiales_educativos
        WHERE id = :materialId
        LIMIT 1
    """)
    suspend fun buscarPorId(
        materialId: Int
    ): MaterialEducativo?

    @Query("""
        UPDATE materiales_educativos
        SET estado = 'EN_REVISION',
            observacionUtp = '',
            fechaRevision = NULL
        WHERE id = :materialId
          AND profesorId = :profesorId
          AND estado IN ('BORRADOR', 'DEVUELTO')
    """)
    suspend fun enviarARevision(
        materialId: Int,
        profesorId: Int
    ): Int

    @Query("""
        UPDATE materiales_educativos
        SET estado = 'APROBADO',
            observacionUtp = :observacion,
            fechaRevision = :fecha
        WHERE id = :materialId
          AND estado = 'EN_REVISION'
    """)
    suspend fun aprobar(
        materialId: Int,
        observacion: String,
        fecha: Long = System.currentTimeMillis()
    ): Int

    @Query("""
        UPDATE materiales_educativos
        SET estado = 'DEVUELTO',
            observacionUtp = :observacion,
            fechaRevision = :fecha
        WHERE id = :materialId
          AND estado = 'EN_REVISION'
    """)
    suspend fun devolver(
        materialId: Int,
        observacion: String,
        fecha: Long = System.currentTimeMillis()
    ): Int

    @Query("""
        UPDATE materiales_educativos
        SET estado = 'PUBLICADO'
        WHERE id = :materialId
          AND estado = 'APROBADO'
    """)
    suspend fun publicar(materialId: Int): Int
}
