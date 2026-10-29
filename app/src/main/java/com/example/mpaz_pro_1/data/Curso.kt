
package com.example.mpaz_pro_1.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cursos",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Curso(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val nivel: String,
    val seccion: String,
    val activo: Boolean = true
)
