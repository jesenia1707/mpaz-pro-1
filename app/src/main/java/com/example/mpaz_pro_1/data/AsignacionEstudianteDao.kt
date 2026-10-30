package com.example.mpaz_pro_1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AsignacionEstudianteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun asignar(asignacion: AsignacionEstudiante): Long

    @Query("""
        SELECT * FROM asignaciones_estudiante
        WHERE estudianteId = :estudianteId
        LIMIT 1
    """)
    suspend fun obtenerPorEstudiante(estudianteId: Int): AsignacionEstudiante?

    @Query("SELECT * FROM asignaciones_estudiante ORDER BY id DESC")
    suspend fun obtenerTodas(): List<AsignacionEstudiante>

    @Query("""
        SELECT * FROM asignaciones_estudiante
        WHERE cursoId = :cursoId
    """)
    suspend fun obtenerPorCurso(cursoId: Int): List<AsignacionEstudiante>
}