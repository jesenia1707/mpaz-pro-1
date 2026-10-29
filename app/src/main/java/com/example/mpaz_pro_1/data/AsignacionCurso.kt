
package com.example.mpaz_pro_1.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asignaciones_curso",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["profesorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Curso::class,
            parentColumns = ["id"],
            childColumns = ["cursoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profesorId"]),
        Index(value = ["cursoId"]),
        Index(value = ["profesorId", "cursoId"], unique = true)
    ]
)
data class AsignacionCurso(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profesorId: Int,
    val cursoId: Int
)
