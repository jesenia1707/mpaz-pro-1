package com.example.mpaz_pro_1

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val Fondo = Color(0xFFFFF9F0)
private val Azul = Color(0xFF3985F7)
private val Rosado = Color(0xFFFF6B9A)
private val Amarillo = Color(0xFFFFC94A)
private val Verde = Color(0xFF43C987)
private val Morado = Color(0xFF9670F5)
private val TextoOscuro = Color(0xFF30344A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = com.example.mpaz_pro_1.data.AppDatabase.getDatabase(this)
        val authRepository = com.example.mpaz_pro_1.data.AuthRepository(
            database.usuarioDao()
        )
        val registroRepository = com.example.mpaz_pro_1.data.RegistroRepository(
            database.usuarioDao()
        )
        val materialRepository = com.example.mpaz_pro_1.data.MaterialRepository(
            materialDao = database.materialEducativoDao(),
            asignacionCursoDao = database.asignacionCursoDao()
        )

        setContent {
            MaterialTheme {
                MPAZApp(
                    authRepository = authRepository,
                    registroRepository = registroRepository,
                    materialRepository = materialRepository,
                    asignacionCursoDao = database.asignacionCursoDao(),
                    usuarioDao = database.usuarioDao(),
                    cursoDao = database.cursoDao(),
                    asignacionEstudianteDao = database.asignacionEstudianteDao(),
                    materialEducativoDao = database.materialEducativoDao()
                )
            }
        }
    }
}

@Composable
fun MPAZApp(
    authRepository: com.example.mpaz_pro_1.data.AuthRepository,
    registroRepository: com.example.mpaz_pro_1.data.RegistroRepository,
    materialRepository: com.example.mpaz_pro_1.data.MaterialRepository,
    asignacionCursoDao: com.example.mpaz_pro_1.data.AsignacionCursoDao,
    usuarioDao: com.example.mpaz_pro_1.data.UsuarioDao,
    cursoDao: com.example.mpaz_pro_1.data.CursoDao,
    asignacionEstudianteDao: com.example.mpaz_pro_1.data.AsignacionEstudianteDao,
    materialEducativoDao: com.example.mpaz_pro_1.data.MaterialEducativoDao
) {
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var usuarioId by remember { mutableStateOf(0) }
    var perfil by remember { mutableStateOf("Estudiante") }
    var pantallaActual by remember { mutableStateOf("LOGIN") }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var mensajeEsError by remember { mutableStateOf(true) }

    fun cerrarSesion() {
        nombre = ""
        usuarioId = 0
        perfil = "Estudiante"
        mensaje = ""
        pantallaActual = "LOGIN"
    }

    when (pantallaActual) {
        "LOGIN" -> PantallaLogin(
            modoRegistro = false,
            cargando = cargando,
            mensaje = mensaje,
            mensajeEsError = mensajeEsError,
            onCambiarModo = { registro ->
                mensaje = ""
                pantallaActual = if (registro) "REGISTRO" else "LOGIN"
            },
            onIngresar = { correo, clave, rol ->
                if (!cargando) {
                    scope.launch {
                        cargando = true
                        mensaje = ""
                        try {
                            when (
                                val resultado = authRepository.iniciarSesion(
                                    correo = correo.trim(),
                                    clave = clave,
                                    rolSeleccionado = rol
                                )
                            ) {
                                is com.example.mpaz_pro_1.data.ResultadoLogin.Exito -> {
                                    nombre = resultado.usuario.nombre
                                    usuarioId = resultado.usuario.id
                                    val rolGuardado = resultado.usuario.rol.trim()
                                    perfil = when {
                                        rolGuardado.equals("Estudiante", ignoreCase = true) -> "Estudiante"
                                        rolGuardado.equals("Profesor", ignoreCase = true) ||
                                                rolGuardado.equals("Docente", ignoreCase = true) -> "Profesor"
                                        rolGuardado.equals("UTP", ignoreCase = true) ||
                                                rolGuardado.equals("Administrador", ignoreCase = true) -> "UTP"
                                        else -> rolGuardado
                                    }
                                    pantallaActual = when (perfil) {
                                        "Estudiante" -> "ESTUDIANTE"
                                        "Profesor" -> "PROFESOR"
                                        "UTP" -> "UTP"
                                        else -> {
                                            mensaje = "El rol de esta cuenta no es válido: $rolGuardado"
                                            mensajeEsError = true
                                            "LOGIN"
                                        }
                                    }
                                }
                                is com.example.mpaz_pro_1.data.ResultadoLogin.Error -> {
                                    mensaje = resultado.mensaje
                                    mensajeEsError = true
                                }
                            }
                        } catch (e: Exception) {
                            mensaje = e.localizedMessage
                                ?: "No se pudo iniciar sesión. Inténtalo nuevamente."
                            mensajeEsError = true
                        } finally {
                            cargando = false
                        }
                    }
                }
            },
            onRegistrar = { _, _, _, _ -> },
            onCrearUtp = {
                mensaje = ""
                pantallaActual = "CREAR_UTP"
            }
        )

        "REGISTRO" -> PantallaLogin(
            modoRegistro = true,
            cargando = cargando,
            mensaje = mensaje,
            mensajeEsError = mensajeEsError,
            onCambiarModo = { registro ->
                mensaje = ""
                pantallaActual = if (registro) "REGISTRO" else "LOGIN"
            },
            onIngresar = { _, _, _ -> pantallaActual = "LOGIN" },
            onRegistrar = { nombreNuevo, correo, clave, rol ->
                if (!cargando) {
                    scope.launch {
                        cargando = true
                        mensaje = ""
                        try {
                            when (
                                val resultado = registroRepository.registrarUsuario(
                                    nombre = nombreNuevo.trim(),
                                    correo = correo.trim(),
                                    clave = clave,
                                    rol = rol
                                )
                            ) {
                                com.example.mpaz_pro_1.data.ResultadoRegistro.Exito -> {
                                    mensaje = "Cuenta creada. Ahora puedes iniciar sesión."
                                    mensajeEsError = false
                                    pantallaActual = "LOGIN"
                                }
                                is com.example.mpaz_pro_1.data.ResultadoRegistro.Error -> {
                                    mensaje = resultado.mensaje
                                    mensajeEsError = true
                                }
                            }
                        } catch (e: Exception) {
                            mensaje = e.localizedMessage
                                ?: "No se pudo crear la cuenta. Inténtalo nuevamente."
                            mensajeEsError = true
                        } finally {
                            cargando = false
                        }
                    }
                }
            }
        )

        "ESTUDIANTE" -> PantallaInicio(
            nombre = nombre,
            estudianteId = usuarioId,
            asignacionEstudianteDao = asignacionEstudianteDao,
            cursoDao = cursoDao,
            materialEducativoDao = materialEducativoDao,
            onCerrarSesion = { cerrarSesion() }
        )

        "PROFESOR" -> PantallaProfesor(
            profesorId = usuarioId,
            materialRepository = materialRepository,
            asignacionCursoDao = asignacionCursoDao,
            onCerrarSesion = { cerrarSesion() },
            onAbrirMisCursos = { pantallaActual = "MIS_CURSOS" }
        )

        "MIS_CURSOS" -> PantallaMisCursos(
            onVolver = { pantallaActual = "PROFESOR" }
        )

        "UTP" -> PantallaUTP(
            usuarioDao = usuarioDao,
            registroRepository = registroRepository,
            cursoDao = cursoDao,
            materialRepository = materialRepository,
            asignacionEstudianteDao = asignacionEstudianteDao,
            onCerrarSesion = { cerrarSesion() }
        )

        "CREAR_UTP" -> PantallaCrearCuentaUTP(
            cargando = cargando,
            mensaje = mensaje,
            mensajeEsError = mensajeEsError,
            onVolver = {
                mensaje = ""
                pantallaActual = "LOGIN"
            },
            onCrear = { nombreNuevo, correo, clave ->
                if (!cargando) {
                    scope.launch {
                        cargando = true
                        mensaje = ""
                        try {
                            when (
                                val resultado = registroRepository.registrarUsuario(
                                    nombre = nombreNuevo.trim(),
                                    correo = correo.trim(),
                                    clave = clave,
                                    rol = "UTP"
                                )
                            ) {
                                com.example.mpaz_pro_1.data.ResultadoRegistro.Exito -> {
                                    mensaje = "Cuenta UTP creada. Ya puedes iniciar sesión."
                                    mensajeEsError = false
                                    pantallaActual = "LOGIN"
                                }
                                is com.example.mpaz_pro_1.data.ResultadoRegistro.Error -> {
                                    mensaje = resultado.mensaje
                                    mensajeEsError = true
                                }
                            }
                        } catch (e: Exception) {
                            mensaje = e.localizedMessage
                                ?: "No se pudo crear la cuenta UTP."
                            mensajeEsError = true
                        } finally {
                            cargando = false
                        }
                    }
                }
            }
        )

        else -> {
            LaunchedEffect(Unit) { cerrarSesion() }
            CircularProgressIndicator()
        }
    }
}

// LOGIN Y REGISTRO

@Composable
fun PantallaLogin(
    modoRegistro: Boolean,
    cargando: Boolean,
    mensaje: String,
    mensajeEsError: Boolean,
    onCambiarModo: (Boolean) -> Unit,
    onIngresar: (String, String, String) -> Unit,
    onRegistrar: (String, String, String, String) -> Unit,
    onCrearUtp: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf("Estudiante") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌈", fontSize = 62.sp)

        Text(
            text = "MPAZ PRO 1.º",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Azul
        )

        Text(
            text = "¡Aprender es una aventura!",
            fontSize = 15.sp,
            color = TextoOscuro
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(22.dp)) {
                Text(
                    text = if (modoRegistro) "¡Crea tu cuenta! ✨" else "¡Hola! 👋",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoOscuro
                )

                Text(
                    text = if (modoRegistro) {
                        "Regístrate para comenzar a aprender"
                    } else {
                        "Ingresa para comenzar a aprender"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(18.dp))

                if (modoRegistro) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre completo") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Text(
                    text = if (modoRegistro) "Selecciona tu perfil" else "Selecciona tu perfil de acceso",
                    fontWeight = FontWeight.Bold
                )

                // UTP no se puede crear desde el registro público.
                val perfilesDisponibles = if (modoRegistro) {
                    listOf("Estudiante", "Profesor")
                } else {
                    listOf("Estudiante", "Profesor", "UTP")
                }

                perfilesDisponibles.forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { perfil = opcion }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = perfil == opcion,
                            onClick = { perfil = opcion },
                            colors = RadioButtonDefaults.colors(selectedColor = Azul)
                        )
                        Text(
                            text = when (opcion) {
                                "Estudiante" -> "🧒 Estudiante"
                                "Profesor" -> "👩‍🏫 Profesor"
                                else -> "📚 UTP / Administrador"
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("ejemplo@correo.cl") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    )
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    )
                )

                if (mensaje.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = mensaje,
                        color = if (mensajeEsError) Color(0xFFD32F2F) else Color(0xFF258C58),
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (modoRegistro) {
                            onRegistrar(nombre, correo, clave, perfil)
                        } else {
                            onIngresar(correo, clave, perfil)
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (modoRegistro) "Crear cuenta ✨" else "¡Entrar a aprender! 🚀",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = { onCambiarModo(!modoRegistro) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                ) {
                    Text(
                        if (modoRegistro) {
                            "Ya tengo una cuenta · Iniciar sesión"
                        } else {
                            "¿No tienes cuenta? · Crear cuenta"
                        },
                        textAlign = TextAlign.Center
                    )
                }

                if (!modoRegistro) {
                    TextButton(
                        onClick = onCrearUtp,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !cargando
                    ) {
                        Text("Crear cuenta UTP de prueba")
                    }
                }

                if (modoRegistro) {
                    Text(
                        text = "Por seguridad, las cuentas UTP deben ser creadas por un administrador autorizado.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "Tus credenciales se validan con la base de datos local.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// INICIO DEL ESTUDIANTE

@Composable
fun PantallaInicio(
    nombre: String,
    estudianteId: Int,
    asignacionEstudianteDao: com.example.mpaz_pro_1.data.AsignacionEstudianteDao,
    cursoDao: com.example.mpaz_pro_1.data.CursoDao,
    materialEducativoDao: com.example.mpaz_pro_1.data.MaterialEducativoDao,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var seccion by remember { mutableStateOf("Inicio") }
    var cursoEstudiante by remember { mutableStateOf<com.example.mpaz_pro_1.data.Curso?>(null) }
    var materialesPublicados by remember { mutableStateOf(emptyList<com.example.mpaz_pro_1.data.MaterialEducativo>()) }
    var cargandoMateriales by remember { mutableStateOf(false) }
    var mensajeMateriales by remember { mutableStateOf("") }

    suspend fun cargarMaterialesEstudiante() {
        cargandoMateriales = true
        mensajeMateriales = ""
        try {
            val asignacion = asignacionEstudianteDao.obtenerPorEstudiante(estudianteId)
            if (asignacion == null) {
                cursoEstudiante = null
                materialesPublicados = emptyList()
            } else {
                cursoEstudiante = cursoDao.buscarPorId(asignacion.cursoId)
                materialesPublicados = materialEducativoDao.obtenerPublicadosPorCurso(asignacion.cursoId)
            }
        } catch (e: Exception) {
            mensajeMateriales = e.localizedMessage ?: "No se pudieron cargar los materiales."
        } finally {
            cargandoMateriales = false
        }
    }

    LaunchedEffect(estudianteId) {
        cargarMaterialesEstudiante()
    }
    var asignaturaSeleccionada by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf("") }
    var temaSeleccionado by remember { mutableStateOf("") }

// Contenido educativo local de ejemplo. Más adelante se podrá administrar desde UTP/profesor.
    val contenidos = mapOf(
        "Lenguaje y Comunicación" to listOf(
            "Unidad 1: Aprendo a leer" to listOf("Las vocales", "Conozco las consonantes", "Formo sílabas"),
            "Unidad 2: Comprendo historias" to listOf("Personajes y lugares", "Ordeno una historia", "Comprensión lectora")
        ),
        "Matemática" to listOf(
            "Unidad 1: Números" to listOf("Cuento y represento números", "Comparo: mayor y menor", "Ordeno números"),
            "Unidad 2: Sumo y resto" to listOf("La suma", "La resta", "Resuelvo problemas")
        ),
        "Ciencias Naturales" to listOf(
            "Unidad 1: Mi cuerpo" to listOf("Partes del cuerpo", "Los sentidos", "Cuido mi salud"),
            "Unidad 2: Seres vivos" to listOf("Animales", "Plantas", "Necesidades de los seres vivos")
        ),
        "Historia, Geografía y Ciencias Sociales" to listOf(
            "Unidad 1: Mi entorno" to listOf("Mi familia", "Mi escuela", "Normas de convivencia"),
            "Unidad 2: Nuestro país" to listOf("Chile y sus símbolos", "Paisajes de Chile", "Mi comunidad")
        )
    )

    fun volverAAsignaturas() {
        asignaturaSeleccionada = ""
        unidadSeleccionada = ""
        temaSeleccionado = ""
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Fondo) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                when (seccion) {
                    "Inicio" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Azul, RoundedCornerShape(17.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text("🌈", fontSize = 28.sp) }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("MPAZ PRO 1.º", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
                                Text("¡Aprender es una aventura!", fontSize = 12.sp, color = TextoOscuro)
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = Azul)
                        ) {
                            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("¡Hola, $nombre! 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(Modifier.height(6.dp))
                                    Text("¿Qué aprenderemos hoy?", color = Color.White)
                                    Spacer(Modifier.height(8.dp))
                                    Text("¡Vamos a jugar y aprender! 🚀", color = Color.White)
                                }
                                Text("🧒", fontSize = 48.sp)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text("¡Elige tu aventura! 🚀", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = TextoOscuro)
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TarjetaAsignatura("📚", "Lenguaje", "¡Lee y descubre!", Rosado, Modifier.weight(1f)) {
                                asignaturaSeleccionada = "Lenguaje y Comunicación"; unidadSeleccionada = ""; temaSeleccionado = ""; seccion = "Aprender"
                            }
                            TarjetaAsignatura("🔢", "Matemática", "¡Juega con números!", Amarillo, Modifier.weight(1f)) {
                                asignaturaSeleccionada = "Matemática"; unidadSeleccionada = ""; temaSeleccionado = ""; seccion = "Aprender"
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TarjetaAsignatura("🌱", "Ciencias", "¡Explora el mundo!", Verde, Modifier.weight(1f)) {
                                asignaturaSeleccionada = "Ciencias Naturales"; unidadSeleccionada = ""; temaSeleccionado = ""; seccion = "Aprender"
                            }
                            TarjetaAsignatura("🌎", "Historia", "¡Conoce tu mundo!", Morado, Modifier.weight(1f)) {
                                asignaturaSeleccionada = "Historia, Geografía y Ciencias Sociales"; unidadSeleccionada = ""; temaSeleccionado = ""; seccion = "Aprender"
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text("⭐ ¡Miro aparecerá aquí cuando completes actividades.", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                    "Aprender" -> {
                        Text("📖 Aprender", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
                        Spacer(Modifier.height(8.dp))
                        if (asignaturaSeleccionada.isBlank()) {
                            Text("Selecciona una asignatura para ver sus unidades.", color = TextoOscuro)
                            Spacer(Modifier.height(18.dp))
                            TarjetaPanel("📚", "Lenguaje y Comunicación", "Lectura, escritura y comprensión.", Rosado) { asignaturaSeleccionada = "Lenguaje y Comunicación" }
                            TarjetaPanel("🔢", "Matemática", "Números y resolución de problemas.", Amarillo) { asignaturaSeleccionada = "Matemática" }
                            TarjetaPanel("🌱", "Ciencias Naturales", "Explora y descubre el mundo.", Verde) { asignaturaSeleccionada = "Ciencias Naturales" }
                            TarjetaPanel("🌎", "Historia y Geografía", "Conoce la historia y nuestro entorno.", Morado) { asignaturaSeleccionada = "Historia, Geografía y Ciencias Sociales" }
                        } else {
                            Text(asignaturaSeleccionada, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                            TextButton(onClick = { volverAAsignaturas() }) { Text("← Todas las asignaturas") }
                            val unidades = contenidos[asignaturaSeleccionada].orEmpty()
                            if (unidadSeleccionada.isBlank()) {
                                Text("Selecciona una unidad:", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(12.dp))
                                unidades.forEachIndexed { indice, unidad ->
                                    TarjetaPanel("📘", unidad.first, "${unidad.second.size} temas para aprender", if (indice % 2 == 0) Color(0xFFDCEAFF) else Color(0xFFE9DEFF)) {
                                        unidadSeleccionada = unidad.first
                                    }
                                }
                            } else if (temaSeleccionado.isBlank()) {
                                TextButton(onClick = { unidadSeleccionada = "" }) { Text("← Volver a unidades") }
                                Text(unidadSeleccionada, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Azul)
                                Spacer(Modifier.height(12.dp))
                                val temas = unidades.firstOrNull { it.first == unidadSeleccionada }?.second.orEmpty()
                                temas.forEachIndexed { indice, tema ->
                                    TarjetaPanel("✏️", "Tema ${indice + 1}: $tema", "Toca para revisar el contenido.", Color(0xFFFFF0CC)) {
                                        temaSeleccionado = tema
                                    }
                                }
                            } else {
                                TextButton(onClick = { temaSeleccionado = "" }) { Text("← Volver a temas") }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(Modifier.padding(20.dp)) {
                                        Text("🌟 $temaSeleccionado", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Azul)
                                        Spacer(Modifier.height(12.dp))
                                        Text("¡Bienvenido/a a este tema!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Aquí podrás revisar la explicación y aprender paso a paso. Este contenido es una demostración local; más adelante se incorporarán los materiales y actividades reales.", color = TextoOscuro)
                                        Spacer(Modifier.height(16.dp))
                                        Text("📎 Materiales", fontWeight = FontWeight.Bold, color = Azul)
                                        Text("Aún no hay archivos adjuntos para este tema.", color = Color.Gray)
                                        Spacer(Modifier.height(12.dp))
                                        Text("📝 Actividad", fontWeight = FontWeight.Bold, color = Azul)
                                        Text("Las actividades de práctica se incorporarán en la siguiente etapa.", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                    "Materiales" -> {
                        Text("📚 Mis materiales", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            cursoEstudiante?.let { "Curso: ${it.nombre} · ${it.nivel} ${it.seccion}" }
                                ?: "Todavía no tienes un curso asignado.",
                            color = TextoOscuro
                        )
                        Spacer(Modifier.height(14.dp))
                        when {
                            cargandoMateriales -> CircularProgressIndicator()
                            mensajeMateriales.isNotBlank() -> Text(mensajeMateriales, color = Color(0xFFD32F2F))
                            cursoEstudiante == null -> Text("Solicita a UTP que te asigne a un curso para recibir tus materiales.", color = Color.Gray)
                            materialesPublicados.isEmpty() -> Text("Todavía no hay PDF publicados para tu curso. Cuando tu profesor los publique, aparecerán aquí.", color = Color.Gray)
                            else -> materialesPublicados.forEach { material ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(material.tema, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                                        Spacer(Modifier.height(4.dp))
                                        Text("${material.asignatura} · ${material.unidad}", color = Azul)
                                        if (material.descripcion.isNotBlank()) {
                                            Spacer(Modifier.height(5.dp))
                                            Text(material.descripcion, color = TextoOscuro)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text("📎 ${material.nombreArchivo}", fontSize = 13.sp)
                                        Spacer(Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                try {
                                                    val archivo = File(material.rutaArchivo)
                                                    if (!archivo.exists()) {
                                                        mensajeMateriales = "No se encontró el archivo PDF en el dispositivo."
                                                    } else {
                                                        val uri = FileProvider.getUriForFile(
                                                            context,
                                                            "${context.packageName}.fileprovider",
                                                            archivo
                                                        )
                                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                                            setDataAndType(uri, "application/pdf")
                                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        }
                                                        context.startActivity(intent)
                                                    }
                                                } catch (e: Exception) {
                                                    mensajeMateriales = e.localizedMessage ?: "No se pudo abrir el PDF. Verifica que haya un lector de PDF instalado."
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Verde)
                                        ) { Text("Abrir PDF") }
                                    }
                                }
                            }
                        }
                        if (mensajeMateriales.isNotBlank() && !cargandoMateriales) {
                            Spacer(Modifier.height(8.dp))
                            Text(mensajeMateriales, color = Color(0xFFD32F2F), fontSize = 12.sp)
                        }
                        TextButton(onClick = { scope.launch { cargarMaterialesEstudiante() } }) {
                            Text("Actualizar materiales")
                        }
                    }
                    "Mis logros" -> {
                        Text("🏆 Mis logros", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Morado)
                        Spacer(Modifier.height(10.dp))
                        Text("¡Descubre todo lo que has conseguido!", color = TextoOscuro)
                        Spacer(Modifier.height(20.dp))
                        TarjetaPanel("⭐", "Mis actividades", "Consulta tus actividades completadas.", Amarillo) { temaSeleccionado = "Mis actividades" }
                        TarjetaPanel("📝", "Mis evaluaciones", "Consulta tus evaluaciones y resultados.", Rosado) { temaSeleccionado = "Mis evaluaciones" }
                        TarjetaPanel("📊", "Mi progreso", "Revisa tus avances de aprendizaje.", Verde) { temaSeleccionado = "Mi progreso" }
                        if (temaSeleccionado.isNotBlank()) {
                            Spacer(Modifier.height(16.dp))
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(Modifier.padding(18.dp)) {
                                    Text(temaSeleccionado, fontWeight = FontWeight.Bold, color = Azul)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Aquí se mostrarán tus resultados cuando estén implementados.")
                                }
                            }
                        }
                    }
                    "Perfil" -> {
                        Text("😊 Mi perfil", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Morado)
                        Spacer(Modifier.height(20.dp))
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(20.dp)) {
                                Text("👤 Información personal", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Azul)
                                Spacer(Modifier.height(16.dp))
                                Text("Nombre completo", color = Color.Gray)
                                Text(nombre, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Tipo de usuario", color = Color.Gray)
                                Text("Estudiante", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Azul)) {
                            Text("Cerrar sesión", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            NavigationBar(containerColor = Color.White) {
                val opciones = listOf("Inicio" to "🏠", "Aprender" to "📖", "Materiales" to "📚", "Mis logros" to "🏆", "Perfil" to "😊")
                opciones.forEach { (nombreOpcion, emoji) ->
                    NavigationBarItem(
                        selected = seccion == nombreOpcion,
                        onClick = {
                            seccion = nombreOpcion
                            if (nombreOpcion == "Materiales") { scope.launch { cargarMaterialesEstudiante() } }
                            if (nombreOpcion != "Aprender") { asignaturaSeleccionada = ""; unidadSeleccionada = ""; temaSeleccionado = "" }
                            if (nombreOpcion == "Aprender") { unidadSeleccionada = ""; temaSeleccionado = "" }
                        },
                        icon = { Text(emoji, fontSize = 20.sp) },
                        label = { Text(nombreOpcion, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Azul, indicatorColor = Color(0xFFDCEAFF), selectedTextColor = Azul)
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaAsignatura(
    emoji: String,
    titulo: String,
    descripcion: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(164.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 34.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = titulo,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color(0xFF29283A)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = descripcion,
                fontSize = 11.sp,
                color = Color(0xFF393747)
            )
        }
    }
}

// PANEL DEL PROFESOR

@Composable
fun PantallaProfesor(
    profesorId: Int,
    materialRepository: com.example.mpaz_pro_1.data.MaterialRepository,
    asignacionCursoDao: com.example.mpaz_pro_1.data.AsignacionCursoDao,
    onCerrarSesion: () -> Unit,
    onAbrirMisCursos: () -> Unit
) {
    var mostrarCarga by remember { mutableStateOf(false) }

    if (mostrarCarga) {
        PantallaCargaMaterial(
            profesorId = profesorId,
            materialRepository = materialRepository,
            asignacionCursoDao = asignacionCursoDao,
            onVolver = { mostrarCarga = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("👩‍🏫", fontSize = 55.sp)
        Text(
            "¡Hola, profesor/a!",
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Azul
        )
        Text("Panel del profesor", fontSize = 17.sp)

        Spacer(Modifier.height(22.dp))
        Text("Tu espacio de trabajo", fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))

        TarjetaPanel(
            "🏫", "Mis cursos",
            "Consulta los cursos que tienes asignados.",
            Color(0xFFFFE0B2)
        ) { onAbrirMisCursos() }

        TarjetaPanel(
            "📚", "Material educativo",
            "Consulta los PDF que UTP te ha asignado y decide cuándo publicarlos.",
            Color(0xFFDCEAFF)
        ) { mostrarCarga = true }

        TarjetaPanel(
            "📝", "Actividades y evaluaciones",
            "Prepara actividades para tus estudiantes.",
            Color(0xFFFFE0ED)
        ) { mostrarFuncionFutura("Actividades y evaluaciones") }

        TarjetaPanel(
            "📊", "Resultados del curso",
            "Consulta los avances de los estudiantes.",
            Color(0xFFD9FBE6)
        ) { mostrarFuncionFutura("Resultados del curso") }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun PantallaCargaMaterial(
    profesorId: Int,
    materialRepository: com.example.mpaz_pro_1.data.MaterialRepository,
    asignacionCursoDao: com.example.mpaz_pro_1.data.AsignacionCursoDao,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var materiales by remember { mutableStateOf(emptyList<com.example.mpaz_pro_1.data.MaterialEducativo>()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    suspend fun cargarMateriales() {
        materiales = materialRepository.obtenerMaterialesProfesor(profesorId)
    }

    LaunchedEffect(profesorId) {
        try {
            cargarMateriales()
        } catch (e: Exception) {
            mensaje = e.localizedMessage ?: "No se pudieron cargar los materiales."
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onVolver) { Text("← Volver al panel") }
        Text("📚 Materiales asignados", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
        Text("UTP te asigna los recursos. Tú decides cuándo compartirlos con tus estudiantes.", color = TextoOscuro)
        Spacer(Modifier.height(18.dp))

        if (cargando) {
            CircularProgressIndicator()
        } else if (materiales.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Todavía no tienes materiales asignados.", fontWeight = FontWeight.Bold)
                    Text("Cuando UTP te asigne un PDF, aparecerá aquí.", color = Color.Gray)
                }
            }
        } else {
            materiales.forEach { material ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(material.tema, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                        Spacer(Modifier.height(4.dp))
                        Text("${material.asignatura} · ${material.unidad}", color = Azul)
                        if (material.descripcion.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(material.descripcion, color = TextoOscuro)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("📎 ${material.nombreArchivo}", fontSize = 13.sp)
                        Text("Estado: ${material.estado}", fontSize = 12.sp, color = Morado)

                        if (material.estado == "BORRADOR") {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                enabled = !cargando,
                                onClick = {
                                    scope.launch {
                                        cargando = true
                                        mensaje = ""
                                        try {
                                            val publicado = materialRepository.publicarMaterialPorProfesor(
                                                materialId = material.id,
                                                profesorId = profesorId
                                            )
                                            mensaje = if (publicado) {
                                                "Material publicado para tus estudiantes."
                                            } else {
                                                "No se pudo publicar. Verifica que el material siga asignado a tu cuenta."
                                            }
                                            cargarMateriales()
                                        } catch (e: Exception) {
                                            mensaje = e.localizedMessage ?: "No se pudo publicar el material."
                                        } finally {
                                            cargando = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Verde)
                            ) { Text("Publicar para estudiantes") }
                        } else if (material.estado == "PUBLICADO") {
                            Text("✓ Visible para estudiantes", color = Verde, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Este material todavía no se puede publicar desde aquí.", color = Color.Gray, fontSize = 12.sp)
                        }
                        if (material.observacionUtp.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text("Observación UTP: ${material.observacionUtp}", color = Color(0xFFD32F2F), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(mensaje, color = Azul)
        }
    }
}

private fun copiarPdfAlAlmacenamiento(context: Context, uri: Uri): Pair<String, String> {
    var nombre = "material_${System.currentTimeMillis()}.pdf"
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) nombre = cursor.getString(index) ?: nombre
        }
    }
    require(nombre.endsWith(".pdf", ignoreCase = true)) { "El archivo seleccionado no es un PDF." }
    val carpeta = File(context.filesDir, "materiales").apply { mkdirs() }
    val destino = File(carpeta, "${System.currentTimeMillis()}_$nombre")
    val input = context.contentResolver.openInputStream(uri)
        ?: throw IllegalStateException("No se pudo abrir el PDF seleccionado.")
    input.use { source ->
        destino.outputStream().use { output -> source.copyTo(output) }
    }
    return nombre to destino.absolutePath
}

@Composable
fun PantallaMisCursos(onVolver: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Fondo
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            TextButton(onClick = onVolver) {
                Text("← Volver al panel")
            }

            Spacer(Modifier.height(12.dp))
            Text("🏫", fontSize = 52.sp)
            Text(
                "Mis cursos",
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Azul
            )
            Text(
                "Consulta los cursos que tienes asignados.",
                fontSize = 16.sp,
                color = TextoOscuro
            )

            Spacer(Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("📚", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Cursos asignados",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Aquí se mostrarán los cursos asignados al profesor.",
                        color = TextoOscuro
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Función futura",
                        color = Azul,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun mostrarFuncionFutura(seccion: String) {
// Las demás secciones se implementarán en los siguientes pasos.
}

// PANEL UTP

@Composable
fun PantallaUTP(
    usuarioDao: com.example.mpaz_pro_1.data.UsuarioDao,
    registroRepository: com.example.mpaz_pro_1.data.RegistroRepository,
    cursoDao: com.example.mpaz_pro_1.data.CursoDao,
    materialRepository: com.example.mpaz_pro_1.data.MaterialRepository,
    asignacionEstudianteDao: com.example.mpaz_pro_1.data.AsignacionEstudianteDao,
    onCerrarSesion: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var seccion by remember { mutableStateOf("Inicio") }
    var usuarios by remember { mutableStateOf(emptyList<com.example.mpaz_pro_1.data.Usuario>()) }
    var filtro by remember { mutableStateOf("Todos") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var nombreNuevo by remember { mutableStateOf("") }
    var correoNuevo by remember { mutableStateOf("") }
    var claveNueva by remember { mutableStateOf("") }
    var rolNuevo by remember { mutableStateOf("Profesor") }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }
    var cursos by remember { mutableStateOf(emptyList<com.example.mpaz_pro_1.data.Curso>()) }
    var mostrarFormularioCurso by remember { mutableStateOf(false) }
    var nombreCurso by remember { mutableStateOf("") }
    var nivelCurso by remember { mutableStateOf("1° Básico") }
    var seccionCurso by remember { mutableStateOf("A") }

    // Formulario de carga de material por UTP
    var profesores by remember { mutableStateOf(emptyList<com.example.mpaz_pro_1.data.Usuario>()) }
    var profesorSeleccionado by remember { mutableStateOf<com.example.mpaz_pro_1.data.Usuario?>(null) }
    var cursoMaterialSeleccionado by remember { mutableStateOf<com.example.mpaz_pro_1.data.Curso?>(null) }
    var menuProfesores by remember { mutableStateOf(false) }
    var menuCursosMaterial by remember { mutableStateOf(false) }
    var asignaturaMaterial by remember { mutableStateOf("") }
    var unidadMaterial by remember { mutableStateOf("") }
    var temaMaterial by remember { mutableStateOf("") }
    var descripcionMaterial by remember { mutableStateOf("") }
    var nombrePdf by remember { mutableStateOf("") }
    var rutaPdf by remember { mutableStateOf("") }
    var guardandoMaterial by remember { mutableStateOf(false) }

    suspend fun cargarUsuarios() {
        usuarios = usuarioDao.obtenerTodos()
    }

    suspend fun cargarCursos() {
        cursos = cursoDao.obtenerActivos()
    }

    LaunchedEffect(seccion) {
        try {
            if (seccion == "Usuarios y roles") cargarUsuarios()
            if (seccion == "Cursos y asignaturas") cargarCursos()
            if (seccion == "Material educativo") {
                cargarUsuarios()
                cargarCursos()
                profesores = usuarios.filter {
                    it.rol.equals("Profesor", ignoreCase = true) || it.rol.equals("Docente", ignoreCase = true)
                }
            }
        } catch (e: Exception) {
            mensaje = e.localizedMessage ?: "No se pudo cargar la información."
            esError = true
        }
    }

    val selectorPdfUtp = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val archivo = withContext(Dispatchers.IO) {
                        copiarPdfAlAlmacenamiento(context, uri)
                    }
                    nombrePdf = archivo.first
                    rutaPdf = archivo.second
                    mensaje = "PDF seleccionado correctamente."
                    esError = false
                } catch (e: Exception) {
                    mensaje = e.localizedMessage ?: "No se pudo copiar el PDF."
                    esError = true
                }
            }
        }
    }

    val usuariosFiltrados = usuarios.filter {
        filtro == "Todos" || it.rol.equals(filtro, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("📚", fontSize = 55.sp)
        Text(
            "¡Hola, UTP!",
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Morado
        )
        Text("Panel de gestión educativa", fontSize = 17.sp, color = TextoOscuro)
        Spacer(Modifier.height(20.dp))

        if (seccion == "Inicio") {
            TarjetaPanel(
                "👥", "Usuarios y roles",
                "Consulta y administra estudiantes y profesores.",
                Color(0xFFDCEAFF)
            ) { seccion = "Usuarios y roles" }

            TarjetaPanel(
                "🏫", "Cursos y asignaturas",
                "Organiza cursos y asignaturas.",
                Color(0xFFFFE0ED)
            ) { seccion = "Cursos y asignaturas" }

            TarjetaPanel(
                "📖", "Material educativo",
                "Carga y organiza recursos educativos.",
                Color(0xFFD9FBE6)
            ) { seccion = "Material educativo" }

            TarjetaPanel(
                "🧒", "Asignación de estudiantes",
                "Inscribe estudiantes en sus cursos y consulta sus asignaciones.",
                Color(0xFFFFE8B3)
            ) { seccion = "Asignación de estudiantes" }

            TarjetaPanel(
                "✅", "Revisión y publicación",
                "Revisa y aprueba material antes de publicarlo.",
                Color(0xFFE9DEFF)
            ) { seccion = "Revisión y publicación" }
        } else {
            TextButton(onClick = {
                seccion = "Inicio"
                mensaje = ""
            }) { Text("← Volver al panel UTP") }

            Text(
                seccion,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Morado
            )
            Spacer(Modifier.height(12.dp))

            if (seccion == "Usuarios y roles") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Usuarios registrados: ${usuarios.size}", fontWeight = FontWeight.SemiBold)
                    Button(onClick = {
                        nombreNuevo = ""
                        correoNuevo = ""
                        claveNueva = ""
                        rolNuevo = "Profesor"
                        mensaje = ""
                        mostrarFormulario = true
                    }) { Text("＋ Crear usuario") }
                }
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Todos", "Estudiante", "Profesor", "UTP").forEach { opcion ->
                        FilterChip(
                            selected = filtro == opcion,
                            onClick = { filtro = opcion },
                            label = { Text(opcion) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (usuariosFiltrados.isEmpty()) {
                    Text("No hay usuarios para mostrar.", color = Color.Gray)
                } else {
                    usuariosFiltrados.forEach { usuario ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(usuario.nombre, fontWeight = FontWeight.Bold, color = TextoOscuro)
                                Text(usuario.correo, color = Color.Gray, fontSize = 13.sp)
                                Text("Perfil: ${usuario.rol}", color = Azul, fontSize = 13.sp)
                            }
                        }
                    }
                }

                if (mostrarFormulario) {
                    AlertDialog(
                        onDismissRequest = { if (!cargando) mostrarFormulario = false },
                        title = { Text("Crear usuario") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = nombreNuevo,
                                    onValueChange = { nombreNuevo = it },
                                    label = { Text("Nombre completo") },
                                    singleLine = true
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = correoNuevo,
                                    onValueChange = { correoNuevo = it },
                                    label = { Text("Correo") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = claveNueva,
                                    onValueChange = { claveNueva = it },
                                    label = { Text("Contraseña (mínimo 8)") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation()
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Perfil")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = rolNuevo == "Profesor",
                                        onClick = { rolNuevo = "Profesor" }
                                    )
                                    Text("Profesor")
                                    RadioButton(
                                        selected = rolNuevo == "Estudiante",
                                        onClick = { rolNuevo = "Estudiante" }
                                    )
                                    Text("Estudiante")
                                }
                                if (mensaje.isNotBlank()) {
                                    Text(
                                        mensaje,
                                        color = if (esError) Color(0xFFD32F2F) else Color(0xFF258C58),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !cargando,
                                onClick = {
                                    scope.launch {
                                        cargando = true
                                        mensaje = ""
                                        try {
                                            when (
                                                val resultado = registroRepository.registrarUsuario(
                                                    nombre = nombreNuevo,
                                                    correo = correoNuevo,
                                                    clave = claveNueva,
                                                    rol = rolNuevo
                                                )
                                            ) {
                                                com.example.mpaz_pro_1.data.ResultadoRegistro.Exito -> {
                                                    cargarUsuarios()
                                                    mostrarFormulario = false
                                                    mensaje = "Usuario creado correctamente."
                                                    esError = false
                                                }
                                                is com.example.mpaz_pro_1.data.ResultadoRegistro.Error -> {
                                                    mensaje = resultado.mensaje
                                                    esError = true
                                                }
                                            }
                                        } catch (e: Exception) {
                                            mensaje = e.localizedMessage ?: "No se pudo crear el usuario."
                                            esError = true
                                        } finally {
                                            cargando = false
                                        }
                                    }
                                }
                            ) { Text(if (cargando) "Guardando..." else "Crear") }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !cargando,
                                onClick = { mostrarFormulario = false; mensaje = "" }
                            ) { Text("Cancelar") }
                        }
                    )
                }
                if (mensaje.isNotBlank() && !mostrarFormulario) {
                    Text(mensaje, color = if (esError) Color(0xFFD32F2F) else Color(0xFF258C58))
                }
            } else if (seccion == "Cursos y asignaturas") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cursos activos: ${cursos.size}", fontWeight = FontWeight.SemiBold)
                    Button(onClick = {
                        nombreCurso = ""
                        nivelCurso = "1° Básico"
                        seccionCurso = "A"
                        mensaje = ""
                        mostrarFormularioCurso = true
                    }) { Text("＋ Crear curso") }
                }
                Spacer(Modifier.height(12.dp))
                if (cursos.isEmpty()) {
                    Text("Todavía no hay cursos activos registrados.", color = Color.Gray)
                } else {
                    cursos.forEach { curso ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(curso.nombre, fontWeight = FontWeight.Bold, color = TextoOscuro)
                                Text("Nivel: ${curso.nivel}  •  Sección: ${curso.seccion}", color = Color.Gray)
                                Text("Estado: Activo", color = Verde, fontSize = 13.sp)
                            }
                        }
                    }
                }
                if (mostrarFormularioCurso) {
                    AlertDialog(
                        onDismissRequest = { if (!cargando) mostrarFormularioCurso = false },
                        title = { Text("Registrar curso") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = nombreCurso,
                                    onValueChange = { nombreCurso = it },
                                    label = { Text("Nombre del curso") },
                                    singleLine = true
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = nivelCurso,
                                    onValueChange = { nivelCurso = it },
                                    label = { Text("Nivel (ej. 1° Básico)") },
                                    singleLine = true
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = seccionCurso,
                                    onValueChange = { seccionCurso = it },
                                    label = { Text("Sección (ej. A)") },
                                    singleLine = true
                                )
                                if (mensaje.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(mensaje, color = if (esError) Color(0xFFD32F2F) else Verde)
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(enabled = !cargando, onClick = {
                                if (nombreCurso.isBlank() || nivelCurso.isBlank() || seccionCurso.isBlank()) {
                                    mensaje = "Completa nombre, nivel y sección."
                                    esError = true
                                } else {
                                    scope.launch {
                                        cargando = true
                                        mensaje = ""
                                        try {
                                            cursoDao.insertar(
                                                com.example.mpaz_pro_1.data.Curso(
                                                    nombre = nombreCurso.trim(),
                                                    nivel = nivelCurso.trim(),
                                                    seccion = seccionCurso.trim().uppercase()
                                                )
                                            )
                                            cargarCursos()
                                            mostrarFormularioCurso = false
                                            mensaje = "Curso registrado correctamente."
                                            esError = false
                                        } catch (e: Exception) {
                                            mensaje = "No se pudo registrar. Verifica que el nombre del curso no esté repetido."
                                            esError = true
                                        } finally {
                                            cargando = false
                                        }
                                    }
                                }
                            }) { Text(if (cargando) "Guardando..." else "Guardar curso") }
                        },
                        dismissButton = {
                            TextButton(enabled = !cargando, onClick = { mostrarFormularioCurso = false; mensaje = "" }) { Text("Cancelar") }
                        }
                    )
                }
                if (mensaje.isNotBlank() && !mostrarFormularioCurso) {
                    Text(mensaje, color = if (esError) Color(0xFFD32F2F) else Color(0xFF258C58))
                }
            } else if (seccion == "Material educativo") {
                Text("Asigna un recurso al profesor y al curso correspondiente.", color = TextoOscuro)
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("1. Seleccionar profesor", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Box {
                            OutlinedButton(onClick = { menuProfesores = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(profesorSeleccionado?.let { it.nombre } ?: "Seleccionar profesor")
                            }
                            DropdownMenu(expanded = menuProfesores, onDismissRequest = { menuProfesores = false }) {
                                profesores.forEach { profesor ->
                                    DropdownMenuItem(
                                        text = { Text("${profesor.nombre} · ${profesor.correo}") },
                                        onClick = { profesorSeleccionado = profesor; menuProfesores = false }
                                    )
                                }
                            }
                        }
                        if (profesores.isEmpty()) {
                            Text("No hay cuentas de profesor registradas.", color = Color(0xFFD32F2F), fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("2. Seleccionar curso", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Box {
                            OutlinedButton(onClick = { menuCursosMaterial = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(cursoMaterialSeleccionado?.let { "${it.nombre} · ${it.nivel} ${it.seccion}" } ?: "Seleccionar curso")
                            }
                            DropdownMenu(expanded = menuCursosMaterial, onDismissRequest = { menuCursosMaterial = false }) {
                                cursos.forEach { curso ->
                                    DropdownMenuItem(
                                        text = { Text("${curso.nombre} · ${curso.nivel} ${curso.seccion}") },
                                        onClick = { cursoMaterialSeleccionado = curso; menuCursosMaterial = false }
                                    )
                                }
                            }
                        }
                        if (cursos.isEmpty()) {
                            Text("Primero registra un curso activo en Cursos y asignaturas.", color = Color(0xFFD32F2F), fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("3. Información del material", fontWeight = FontWeight.Bold)
                        OutlinedTextField(asignaturaMaterial, { asignaturaMaterial = it }, label = { Text("Asignatura") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(unidadMaterial, { unidadMaterial = it }, label = { Text("Unidad") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(temaMaterial, { temaMaterial = it }, label = { Text("Tema") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(descripcionMaterial, { descripcionMaterial = it }, label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = { selectorPdfUtp.launch(arrayOf("application/pdf")) }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (nombrePdf.isBlank()) "Seleccionar archivo PDF" else "📎 $nombrePdf")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("El material quedará como BORRADOR. El profesor decidirá cuándo publicarlo.", color = TextoOscuro, fontSize = 12.sp)
                        Button(
                            enabled = !guardandoMaterial && profesorSeleccionado != null && cursoMaterialSeleccionado != null &&
                                    asignaturaMaterial.isNotBlank() && unidadMaterial.isNotBlank() && temaMaterial.isNotBlank() &&
                                    nombrePdf.isNotBlank() && rutaPdf.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    guardandoMaterial = true
                                    mensaje = ""
                                    try {
                                        materialRepository.crearMaterialDesdeUTP(
                                            rol = "UTP",
                                            profesorId = profesorSeleccionado!!.id,
                                            cursoId = cursoMaterialSeleccionado!!.id,
                                            asignatura = asignaturaMaterial.trim(),
                                            unidad = unidadMaterial.trim(),
                                            tema = temaMaterial.trim(),
                                            descripcion = descripcionMaterial.trim(),
                                            nombreArchivo = nombrePdf,
                                            rutaArchivo = rutaPdf
                                        )
                                        mensaje = "Material asignado correctamente. El profesor ya puede verlo."
                                        esError = false
                                        profesorSeleccionado = null
                                        cursoMaterialSeleccionado = null
                                        asignaturaMaterial = ""
                                        unidadMaterial = ""
                                        temaMaterial = ""
                                        descripcionMaterial = ""
                                        nombrePdf = ""
                                        rutaPdf = ""
                                    } catch (e: Exception) {
                                        mensaje = e.localizedMessage ?: "No se pudo asignar el material."
                                        esError = true
                                    } finally {
                                        guardandoMaterial = false
                                    }
                                }
                            }
                        ) { Text(if (guardandoMaterial) "Guardando..." else "Asignar material al profesor") }
                    }
                }
                if (mensaje.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(mensaje, color = if (esError) Color(0xFFD32F2F) else Color(0xFF258C58))
                }
            } else if (seccion == "Revisión y publicación") {
                Text("La publicación de los materiales asignados queda a cargo de cada profesor.", color = TextoOscuro)
                Text("UTP puede seguir utilizando el flujo de revisión de materiales enviados por profesores, pero no publica los materiales asignados desde esta pantalla.", color = Color.Gray, fontSize = 12.sp)
            } else if (seccion == "Asignación de estudiantes") {
                PantallaAsignacionEstudiantes(
                    usuarioDao = usuarioDao,
                    cursoDao = cursoDao,
                    asignacionEstudianteDao = asignacionEstudianteDao
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun PantallaAsignacionEstudiantes(
    usuarioDao: com.example.mpaz_pro_1.data.UsuarioDao,
    cursoDao: com.example.mpaz_pro_1.data.CursoDao,
    asignacionEstudianteDao: com.example.mpaz_pro_1.data.AsignacionEstudianteDao
) {
    val scope = rememberCoroutineScope()

    var estudiantes by remember {
        mutableStateOf(emptyList<com.example.mpaz_pro_1.data.Usuario>())
    }
    var cursos by remember {
        mutableStateOf(emptyList<com.example.mpaz_pro_1.data.Curso>())
    }
    var asignaciones by remember {
        mutableStateOf(emptyList<com.example.mpaz_pro_1.data.AsignacionEstudiante>())
    }

    var estudianteSeleccionado by remember {
        mutableStateOf<com.example.mpaz_pro_1.data.Usuario?>(null)
    }
    var cursoSeleccionado by remember {
        mutableStateOf<com.example.mpaz_pro_1.data.Curso?>(null)
    }
    var menuEstudiantes by remember { mutableStateOf(false) }
    var menuCursos by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }

    suspend fun cargarDatos() {
        val todosLosUsuarios = usuarioDao.obtenerTodos()
        estudiantes = todosLosUsuarios.filter {
            it.rol.trim().equals("Estudiante", ignoreCase = true)
        }
        cursos = cursoDao.obtenerActivos()
        asignaciones = asignacionEstudianteDao.obtenerTodas()
    }

    LaunchedEffect(Unit) {
        cargando = true
        try {
            cargarDatos()
        } catch (e: Exception) {
            mensaje = e.localizedMessage ?: "No se pudieron cargar los datos."
            esError = true
        } finally {
            cargando = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Asigna cada estudiante a un curso activo. Si ya tenía un curso, la nueva asignación lo reemplazará.",
            color = TextoOscuro,
            fontSize = 13.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("1. Seleccionar estudiante", fontWeight = FontWeight.Bold)

                Box {
                    OutlinedButton(
                        onClick = { menuEstudiantes = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            estudianteSeleccionado?.let { "${it.nombre} · ${it.correo}" }
                                ?: "Seleccionar estudiante"
                        )
                    }

                    DropdownMenu(
                        expanded = menuEstudiantes,
                        onDismissRequest = { menuEstudiantes = false }
                    ) {
                        estudiantes.forEach { estudiante ->
                            DropdownMenuItem(
                                text = { Text("${estudiante.nombre} · ${estudiante.correo}") },
                                onClick = {
                                    estudianteSeleccionado = estudiante
                                    menuEstudiantes = false
                                }
                            )
                        }
                    }
                }

                if (estudiantes.isEmpty() && !cargando) {
                    Text(
                        "No hay estudiantes registrados. Primero crea una cuenta de estudiante.",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp
                    )
                }

                Text("2. Seleccionar curso activo", fontWeight = FontWeight.Bold)

                Box {
                    OutlinedButton(
                        onClick = { menuCursos = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            cursoSeleccionado?.let {
                                "${it.nombre} · ${it.nivel} ${it.seccion}"
                            } ?: "Seleccionar curso"
                        )
                    }

                    DropdownMenu(
                        expanded = menuCursos,
                        onDismissRequest = { menuCursos = false }
                    ) {
                        cursos.forEach { curso ->
                            DropdownMenuItem(
                                text = { Text("${curso.nombre} · ${curso.nivel} ${curso.seccion}") },
                                onClick = {
                                    cursoSeleccionado = curso
                                    menuCursos = false
                                }
                            )
                        }
                    }
                }

                if (cursos.isEmpty() && !cargando) {
                    Text(
                        "No hay cursos activos. Primero crea un curso en Cursos y asignaturas.",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        val estudiante = estudianteSeleccionado
                        val curso = cursoSeleccionado

                        if (estudiante == null || curso == null) {
                            mensaje = "Selecciona un estudiante y un curso."
                            esError = true
                        } else {
                            scope.launch {
                                cargando = true
                                mensaje = ""
                                try {
                                    asignacionEstudianteDao.asignar(
                                        com.example.mpaz_pro_1.data.AsignacionEstudiante(
                                            estudianteId = estudiante.id,
                                            cursoId = curso.id
                                        )
                                    )
                                    cargarDatos()
                                    mensaje = "Estudiante asignado al curso correctamente."
                                    esError = false
                                } catch (e: Exception) {
                                    mensaje = e.localizedMessage
                                        ?: "No se pudo guardar la asignación."
                                    esError = true
                                } finally {
                                    cargando = false
                                }
                            }
                        }
                    },
                    enabled = !cargando &&
                            estudianteSeleccionado != null &&
                            cursoSeleccionado != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (cargando) "Guardando..." else "Asignar estudiante")
                }

                if (mensaje.isNotBlank()) {
                    Text(
                        mensaje,
                        color = if (esError) Color(0xFFD32F2F) else Color(0xFF258C58),
                        fontSize = 13.sp
                    )
                }
            }
        }

        Text(
            "Estudiantes asignados (${asignaciones.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextoOscuro
        )

        if (cargando && asignaciones.isEmpty()) {
            CircularProgressIndicator()
        } else if (asignaciones.isEmpty()) {
            Text("Todavía no hay estudiantes asignados a cursos.", color = Color.Gray)
        } else {
            asignaciones.forEach { asignacion ->
                val estudiante = estudiantes.firstOrNull {
                    it.id == asignacion.estudianteId
                }
                val curso = cursos.firstOrNull {
                    it.id == asignacion.cursoId
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            estudiante?.nombre ?: "Estudiante #${asignacion.estudianteId}",
                            fontWeight = FontWeight.Bold,
                            color = TextoOscuro
                        )
                        Text(
                            curso?.let { "${it.nombre} · ${it.nivel} ${it.seccion}" }
                                ?: "Curso #${asignacion.cursoId}",
                            color = Azul,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaPanel(
    emoji: String,
    titulo: String,
    descripcion: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 34.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextoOscuro
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = TextoOscuro
                )
            }
        }
    }
}

// CREACIÓN TEMPORAL DE CUENTA UTP DE PRUEBA

@Composable
fun PantallaCrearCuentaUTP(
    cargando: Boolean,
    mensaje: String,
    mensajeEsError: Boolean,
    onVolver: () -> Unit,
    onCrear: (String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📚", fontSize = 60.sp)

        Text(
            "Crear cuenta UTP",
            fontSize = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Morado
        )

        Text(
            "Cuenta de prueba para acceder al panel de gestión educativa",
            textAlign = TextAlign.Center,
            color = TextoOscuro
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña (mínimo 8 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )

        Spacer(Modifier.height(12.dp))

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = if (mensajeEsError) Color(0xFFD32F2F) else Color(0xFF258C58),
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = { onCrear(nombre, correo, clave) },
            enabled = !cargando && nombre.isNotBlank() &&
                    correo.isNotBlank() && clave.length >= 8,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Morado)
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Crear cuenta UTP")
            }
        }

        TextButton(
            onClick = onVolver,
            enabled = !cargando
        ) {
            Text("Volver al inicio de sesión")
        }

        Text(
            "Esta opción es temporal para pruebas locales. No debe quedar disponible en una versión pública.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}