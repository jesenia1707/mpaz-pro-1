
package com.example.mpaz_pro_1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CursoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(curso: Curso): Long

    @Query("SELECT * FROM cursos WHERE activo = 1 ORDER BY nivel, seccion")
    suspend fun obtenerActivos(): List<Curso>

    @Query("SELECT * FROM cursos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Curso?

    @Query("SELECT COUNT(*) FROM cursos")
    suspend fun contar(): Int
}
