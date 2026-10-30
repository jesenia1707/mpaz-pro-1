
package com.example.mpaz_pro_1.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asignaciones_estudiante",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["estudianteId"],
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
        Index(value = ["estudianteId"], unique = true),
        Index(value = ["cursoId"])
    ]
)
data class AsignacionEstudiante(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val estudianteId: Int,
    val cursoId: Int
)
