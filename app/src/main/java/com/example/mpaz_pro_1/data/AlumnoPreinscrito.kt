package com.example.mpaz_pro_1.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alumnos_preinscritos",
    foreignKeys = [
        ForeignKey(
            entity = Curso::class,
            parentColumns = ["id"],
            childColumns = ["cursoId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["codigoMatricula"], unique = true),
        Index(value = ["cursoId"])
    ]
)
data class AlumnoPreinscrito(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val codigoMatricula: String,
    val cursoId: Int,
    val usado: Boolean = false
)
