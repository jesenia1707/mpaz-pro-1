package com.example.mpaz_pro_1.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "materiales_educativos",
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
        Index(value = ["estado"])
    ]
)
data class MaterialEducativo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profesorId: Int,
    val cursoId: Int,
    val asignatura: String,
    val unidad: String,
    val tema: String,
    val descripcion: String,
    val nombreArchivo: String,
    val rutaArchivo: String,
    val estado: String = "BORRADOR",
    val observacionUtp: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaRevision: Long? = null
)
