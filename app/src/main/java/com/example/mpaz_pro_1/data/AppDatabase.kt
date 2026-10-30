
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
        AlumnoPreinscrito::class,
        MaterialEducativo::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun cursoDao(): CursoDao

    abstract fun asignacionCursoDao(): AsignacionCursoDao

    abstract fun alumnoPreinscritoDao(): AlumnoPreinscritoDao

    abstract fun materialEducativoDao(): MaterialEducativoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración de la versión 1 a la versión 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS cursos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        nivel TEXT NOT NULL,
                        seccion TEXT NOT NULL,
                        activo INTEGER NOT NULL
                    )
                """)

                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_cursos_nombre
                    ON cursos(nombre)
                """)

                db.execSQL("""
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
                """)

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS
                    index_asignaciones_curso_profesorId
                    ON asignaciones_curso(profesorId)
                """)

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS
                    index_asignaciones_curso_cursoId
                    ON asignaciones_curso(cursoId)
                """)

                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_asignaciones_curso_profesorId_cursoId
                    ON asignaciones_curso(profesorId, cursoId)
                """)

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS alumnos_preinscritos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        codigoMatricula TEXT NOT NULL,
                        cursoId INTEGER NOT NULL,
                        usado INTEGER NOT NULL,
                        FOREIGN KEY(cursoId)
                            REFERENCES cursos(id)
                            ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                """)

                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    index_alumnos_preinscritos_codigoMatricula
                    ON alumnos_preinscritos(codigoMatricula)
                """)

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS
                    index_alumnos_preinscritos_cursoId
                    ON alumnos_preinscritos(cursoId)
                """)
            }
        }

        // Migración de la versión 2 a la versión 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
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
                """)

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS
                    index_materiales_educativos_profesorId
                    ON materiales_educativos(profesorId)
                """)

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS
                    index_materiales_educativos_cursoId
                    ON materiales_educativos(cursoId)
                """)

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS
                    index_materiales_educativos_estado
                    ON materiales_educativos(estado)
                """)
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
                        MIGRATION_2_3
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
