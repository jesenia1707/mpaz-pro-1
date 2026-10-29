
package com.example.mpaz_pro_1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AsignacionCursoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun asignar(asignacion: AsignacionCurso): Long

    @Query("""
        SELECT cursos.*
        FROM cursos
        INNER JOIN asignaciones_curso
            ON cursos.id = asignaciones_curso.cursoId
        WHERE asignaciones_curso.profesorId = :profesorId
          AND cursos.activo = 1
        ORDER BY cursos.nivel, cursos.seccion
    """)
    suspend fun obtenerCursosDelProfesor(profesorId: Int): List<Curso>

    @Query("""
        SELECT COUNT(*)
        FROM asignaciones_curso
        WHERE cursoId = :cursoId
    """)
    suspend fun contarAsignacionesDelCurso(cursoId: Int): Int
}
