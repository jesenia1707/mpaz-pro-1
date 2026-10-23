package com.example.mpaz_pro_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.example.mpaz_pro_1.nav.AppNavigation
import com.example.mpaz_pro_1.nav.Rutas

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

        setContent {
            MaterialTheme {
                MPAZApp(
                    authRepository = authRepository,
                    registroRepository = registroRepository
                )
            }
        }
    }
}

@Composable
fun MPAZApp(
    authRepository: com.example.mpaz_pro_1.data.AuthRepository,
    registroRepository: com.example.mpaz_pro_1.data.RegistroRepository
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf("Estudiante") }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var mensajeEsError by remember { mutableStateOf(true) }

    fun rutaParaRol(rol: String): String = when (rol) {
        "Estudiante" -> Rutas.ESTUDIANTE
        "Profesor" -> Rutas.PROFESOR
        else -> Rutas.UTP
    }

    fun cerrarSesion() {
        nombre = ""
        perfil = "Estudiante"
        mensaje = ""
        navController.navigate(Rutas.LOGIN) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    AppNavigation(
        navController = navController,
        pantallaLogin = {
            PantallaLogin(
                modoRegistro = false,
                cargando = cargando,
                mensaje = mensaje,
                mensajeEsError = mensajeEsError,
                onCambiarModo = { registro ->
                    mensaje = ""
                    navController.navigate(if (registro) Rutas.REGISTRO else Rutas.LOGIN) {
                        launchSingleTop = true
                    }
                },
                onIngresar = { correo, clave, rol ->
                    if (!cargando) {
                        scope.launch {
                            cargando = true
                            mensaje = ""
                            try {
                                when (
                                    val resultado = authRepository.iniciarSesion(
                                        correo = correo,
                                        clave = clave,
                                        rolSeleccionado = rol
                                    )
                                ) {
                                    is com.example.mpaz_pro_1.data.ResultadoLogin.Exito -> {
                                        nombre = resultado.usuario.nombre
                                        perfil = resultado.usuario.rol
                                        mensaje = ""
                                        navController.navigate(rutaParaRol(perfil)) {
                                            popUpTo(Rutas.LOGIN) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                    is com.example.mpaz_pro_1.data.ResultadoLogin.Error -> {
                                        mensaje = resultado.mensaje
                                        mensajeEsError = true
                                    }
                                }
                            } catch (_: Exception) {
                                mensaje = "No se pudo iniciar sesión. Inténtalo nuevamente."
                                mensajeEsError = true
                            } finally {
                                cargando = false
                            }
                        }
                    }
                },
                onRegistrar = { nombreNuevo, correo, clave, rol ->
                    if (!cargando) {
                        scope.launch {
                            cargando = true
                            mensaje = ""
                            try {
                                when (
                                    val resultado = registroRepository.registrarUsuario(
                                        nombre = nombreNuevo,
                                        correo = correo,
                                        clave = clave,
                                        rol = rol
                                    )
                                ) {
                                    com.example.mpaz_pro_1.data.ResultadoRegistro.Exito -> {
                                        mensaje = "Cuenta creada. Ahora puedes iniciar sesión."
                                        mensajeEsError = false
                                        navController.navigate(Rutas.LOGIN) {
                                            popUpTo(Rutas.LOGIN) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                    is com.example.mpaz_pro_1.data.ResultadoRegistro.Error -> {
                                        mensaje = resultado.mensaje
                                        mensajeEsError = true
                                    }
                                }
                            } catch (_: Exception) {
                                mensaje = "No se pudo crear la cuenta. Inténtalo nuevamente."
                                mensajeEsError = true
                            } finally {
                                cargando = false
                            }
                        }
                    }
                }
            )
        },
        pantallaRegistro = {
            PantallaLogin(
                modoRegistro = true,
                cargando = cargando,
                mensaje = mensaje,
                mensajeEsError = mensajeEsError,
                onCambiarModo = { registro ->
                    mensaje = ""
                    navController.navigate(if (registro) Rutas.REGISTRO else Rutas.LOGIN) {
                        launchSingleTop = true
                    }
                },
                onIngresar = { correo, clave, rol ->
                    navController.navigate(Rutas.LOGIN) {
                        launchSingleTop = true
                    }
                },
                onRegistrar = { nombreNuevo, correo, clave, rol ->
                    if (!cargando) {
                        scope.launch {
                            cargando = true
                            mensaje = ""
                            try {
                                when (
                                    val resultado = registroRepository.registrarUsuario(
                                        nombre = nombreNuevo,
                                        correo = correo,
                                        clave = clave,
                                        rol = rol
                                    )
                                ) {
                                    com.example.mpaz_pro_1.data.ResultadoRegistro.Exito -> {
                                        mensaje = "Cuenta creada. Ahora puedes iniciar sesión."
                                        mensajeEsError = false
                                        navController.navigate(Rutas.LOGIN) {
                                            popUpTo(Rutas.LOGIN) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                    is com.example.mpaz_pro_1.data.ResultadoRegistro.Error -> {
                                        mensaje = resultado.mensaje
                                        mensajeEsError = true
                                    }
                                }
                            } catch (_: Exception) {
                                mensaje = "No se pudo crear la cuenta. Inténtalo nuevamente."
                                mensajeEsError = true
                            } finally {
                                cargando = false
                            }
                        }
                    }
                }
            )
        },
        pantallaEstudiante = {
            PantallaInicio(nombre = nombre, onCerrarSesion = { cerrarSesion() })
        },
        pantallaProfesor = {
            PantallaProfesor(
                onCerrarSesion = { cerrarSesion() },
                onAbrirMisCursos = { navController.navigate(Rutas.MIS_CURSOS) }
            )
        },
        pantallaMisCursos = {
            PantallaMisCursos(onVolver = { navController.popBackStack() })
        },
        pantallaUTP = {
            PantallaUTP(onCerrarSesion = { cerrarSesion() })
        }
    )
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
    onRegistrar: (String, String, String, String) -> Unit
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
    onCerrarSesion: () -> Unit
) {
    var seccion by remember { mutableStateOf("Inicio") }
    var asignaturaSeleccionada by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Fondo
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Azul, RoundedCornerShape(17.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌈", fontSize = 28.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "MPAZ PRO 1.º",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Azul
                        )
                        Text(
                            text = "¡Aprender es una aventura!",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Azul
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "¡Hola, $nombre! 👋",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "¿Qué aprenderemos hoy?",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "¡Vamos a jugar y aprender!",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                        Text("🧒", fontSize = 56.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "¡Elige tu aventura! 🚀",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextoOscuro
                )

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaAsignatura(
                        "📚", "Lenguaje",
                        "¡Lee y descubre!",
                        Rosado,
                        Modifier.weight(1f)
                    ) {
                        asignaturaSeleccionada = "Lenguaje y Comunicación"
                    }

                    TarjetaAsignatura(
                        "🔢", "Matemática",
                        "¡Juega con números!",
                        Amarillo,
                        Modifier.weight(1f)
                    ) {
                        asignaturaSeleccionada = "Matemática"
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaAsignatura(
                        "🌱", "Ciencias",
                        "¡Explora el mundo!",
                        Verde,
                        Modifier.weight(1f)
                    ) {
                        asignaturaSeleccionada = "Ciencias Naturales"
                    }

                    TarjetaAsignatura(
                        "🌎", "Historia",
                        "¡Conoce tu mundo!",
                        Morado,
                        Modifier.weight(1f)
                    ) {
                        asignaturaSeleccionada =
                            "Historia, Geografía y Ciencias Sociales"
                    }
                }

                Spacer(Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 27.sp)
                            Spacer(Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "¡Mira cuánto has avanzado!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "3 de 5 actividades completadas",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { 0.6f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = Verde,
                            trackColor = Color(0xFFE8F2E9),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "¡Vas genial! 🌟 60%",
                            color = Color(0xFF258C58),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (asignaturaSeleccionada.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F0FF)
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = asignaturaSeleccionada,
                                fontWeight = FontWeight.Bold,
                                color = Azul
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Aquí agregaremos las unidades, el material y las actividades."
                            )
                            TextButton(
                                onClick = { asignaturaSeleccionada = "" }
                            ) {
                                Text("Volver")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                OutlinedButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }

                Spacer(Modifier.height(12.dp))
            }

            NavigationBar(containerColor = Color.White) {
                val opciones = listOf(
                    "Inicio" to "🏠",
                    "Aprender" to "📖",
                    "Mis logros" to "🏆",
                    "Perfil" to "😊"
                )

                opciones.forEach { (nombreOpcion, emoji) ->
                    NavigationBarItem(
                        selected = seccion == nombreOpcion,
                        onClick = { seccion = nombreOpcion },
                        icon = { Text(emoji, fontSize = 20.sp) },
                        label = { Text(nombreOpcion, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Azul,
                            indicatorColor = Color(0xFFDCEAFF),
                            selectedTextColor = Azul
                        )
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
    onCerrarSesion: () -> Unit,
    onAbrirMisCursos: () -> Unit
) {
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

        Text(
            "Tu espacio de trabajo",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(14.dp))

        TarjetaPanel(
            "🏫", "Mis cursos",
            "Consulta los cursos que tienes asignados.",
            Color(0xFFFFE0B2)
        ) { onAbrirMisCursos() }

        TarjetaPanel(
            "📚", "Material educativo",
            "Consulta el material publicado por UTP.",
            Color(0xFFDCEAFF)
        ) { mostrarFuncionFutura("Material educativo") }

        TarjetaPanel(
            "📝", "Actividades y evaluaciones",
            "Prepara actividades para tus estudiantes.",
            Color(0xFFFFE0ED)
        ) { mostrarFuncionFutura("Actividades y evaluaciones") }

        TarjetaPanel(
            "📊", "Resultados del curso",
            "Aquí mostraremos los avances de los estudiantes.",
            Color(0xFFD9FBE6)
        ) { mostrarFuncionFutura("Resultados del curso") }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
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
fun PantallaUTP(onCerrarSesion: () -> Unit) {
    var seccion by remember { mutableStateOf("Inicio") }

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
        Text("Panel de gestión educativa", fontSize = 17.sp)

        Spacer(Modifier.height(22.dp))

        TarjetaPanel(
            "👥", "Usuarios y roles",
            "Administración de estudiantes y profesores.",
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
            "✅", "Revisión y publicación",
            "Revisa y aprueba material antes de publicarlo.",
            Color(0xFFE9DEFF)
        ) { seccion = "Revisión y publicación" }

        if (seccion != "Inicio") {
            Spacer(Modifier.height(16.dp))
            Text(
                text = seccion,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Morado
            )
            Text("Esta sección se implementará en los siguientes pasos.")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
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

