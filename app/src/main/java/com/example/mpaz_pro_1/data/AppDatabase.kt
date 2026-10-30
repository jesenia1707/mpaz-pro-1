
package com.example.mpaz_pro_1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Usuario::class,
        Curso::class,
        AsignacionCurso::class,
        MaterialEducativo::class,
        AsignacionEstudiante::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun cursoDao(): CursoDao

    abstract fun asignacionCursoDao(): AsignacionCursoDao

    abstract fun materialEducativoDao(): MaterialEducativoDao

    abstract fun asignacionEstudianteDao(): AsignacionEstudianteDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de versión 1 a 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cursos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        nivel TEXT NOT NULL,
                        seccion TEXT NOT NULL,
                        activo INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS asignaciones_curso (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profesorId INTEGER NOT NULL,
                        cursoId INTEGER NOT NULL,
                        FOREIGN KEY(profesorId)
                            REFERENCES usuarios(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(cursoId)
                            REFERENCES cursos(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_asignaciones_curso_profesorId
                    ON asignaciones_curso(profesorId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_asignaciones_curso_cursoId
                    ON asignaciones_curso(cursoId)
                    """.trimIndent()
                )
            }
        }

        // Migración de versión 2 a 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS materiales_educativos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        profesorId INTEGER NOT NULL,
                        cursoId INTEGER NOT NULL,
                        asignatura TEXT NOT NULL,
                        unidad TEXT NOT NULL,
                        tema TEXT NOT NULL,
                        descripcion TEXT NOT NULL,
                        nombreArchivo TEXT NOT NULL,
                        rutaArchivo TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        observacionUtp TEXT NOT NULL,
                        fechaCreacion INTEGER NOT NULL,
                        fechaRevision INTEGER,
                        FOREIGN KEY(profesorId)
                            REFERENCES usuarios(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(cursoId)
                            REFERENCES cursos(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_materiales_educativos_profesorId
                    ON materiales_educativos(profesorId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_materiales_educativos_cursoId
                    ON materiales_educativos(cursoId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_materiales_educativos_estado
                    ON materiales_educativos(estado)
                    """.trimIndent()
                )
            }
        }

        // Migración de versión 3 a 4
        // Incorpora la asignación de estudiantes a cursos.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS asignaciones_estudiante (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        estudianteId INTEGER NOT NULL,
                        cursoId INTEGER NOT NULL,
                        FOREIGN KEY(estudianteId)
                            REFERENCES usuarios(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(cursoId)
                            REFERENCES cursos(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_asignaciones_estudiante_estudianteId
                    ON asignaciones_estudiante(estudianteId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_asignaciones_estudiante_cursoId
                    ON asignaciones_estudiante(cursoId)
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
