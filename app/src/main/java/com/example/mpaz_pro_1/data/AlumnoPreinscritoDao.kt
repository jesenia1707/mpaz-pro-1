package com.example.mpaz_pro_1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlumnoPreinscritoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(
        alumno: AlumnoPreinscrito
    ): Long

    @Query(
        "SELECT * FROM alumnos_preinscritos " +
                "WHERE codigoMatricula = :codigo LIMIT 1"
    )
    suspend fun buscarPorCodigo(
        codigo: String
    ): AlumnoPreinscrito?

    @Query(
        "UPDATE alumnos_preinscritos " +
                "SET usado = 1 WHERE id = :id AND usado = 0"
    )
    suspend fun marcarComoUsado(id: Int): Int

    @Query(
        "SELECT * FROM alumnos_preinscritos " +
                "WHERE usado = 0 ORDER BY nombre"
    )
    suspend fun obtenerPendientes(): List<AlumnoPreinscrito>

    @Query(
        "SELECT COUNT(*) FROM alumnos_preinscritos " +
                "WHERE cursoId = :cursoId"
    )
    suspend fun contarPorCurso(cursoId: Int): Int
}
