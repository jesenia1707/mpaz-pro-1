package com.example.mpaz_pro_1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mpaz_pro_1.ui.components.TarjetaAsignatura
import com.example.mpaz_pro_1.ui.theme.Amarillo
import com.example.mpaz_pro_1.ui.theme.Azul
import com.example.mpaz_pro_1.ui.theme.AzulSuave
import com.example.mpaz_pro_1.ui.theme.Blanco
import com.example.mpaz_pro_1.ui.theme.Fondo
import com.example.mpaz_pro_1.ui.theme.Morado
import com.example.mpaz_pro_1.ui.theme.Rosado
import com.example.mpaz_pro_1.ui.theme.Tinta
import com.example.mpaz_pro_1.ui.theme.TintaSuave
import com.example.mpaz_pro_1.ui.theme.Verde
import com.example.mpaz_pro_1.ui.theme.VerdeClaro
import com.example.mpaz_pro_1.ui.theme.VerdeOscuro
import com.example.mpaz_pro_1.ui.theme.Mpazpro1Theme

@Composable
fun PantallaInicio() {
    var seccion by rememberSaveable { mutableStateOf("Inicio") }
    var asignaturaSeleccionada by rememberSaveable { mutableStateOf("") }

    Scaffold(
        containerColor = Fondo,
        bottomBar = {
            NavigationBar(containerColor = Blanco, tonalElevation = 0.dp) {
                val opciones = listOf(
                    "Inicio" to "🏠",
                    "Aprender" to "📖",
                    "Mis logros" to "🏆",
                    "Perfil" to "😊"
                )

                opciones.forEach { (nombre, emoji) ->
                    NavigationBarItem(
                        selected = seccion == nombre,
                        onClick = { seccion = nombre },
                        icon = { Text(emoji, fontSize = 20.sp) },
                        label = { Text(nombre, style = MaterialTheme.typography.bodySmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Azul,
                            indicatorColor = AzulSuave,
                            selectedTextColor = Azul,
                            unselectedIconColor = TintaSuave,
                            unselectedTextColor = TintaSuave
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            Encabezado()

            Spacer(Modifier.height(24.dp))

            TarjetaSaludo()

            Spacer(Modifier.height(25.dp))

            Text(
                text = "¡Elige tu aventura! 🚀",
                style = MaterialTheme.typography.titleLarge,
                color = Tinta
            )

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaAsignatura(
                    emoji = "📚",
                    titulo = "Lenguaje",
                    descripcion = "¡Lee y descubre!",
                    color = Rosado,
                    modifier = Modifier.weight(1f)
                ) {
                    asignaturaSeleccionada = "Lenguaje y Comunicación"
                }

                TarjetaAsignatura(
                    emoji = "🔢",
                    titulo = "Matemática",
                    descripcion = "¡Juega con números!",
                    color = Amarillo,
                    modifier = Modifier.weight(1f)
                ) {
                    asignaturaSeleccionada = "Matemática"
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaAsignatura(
                    emoji = "🌱",
                    titulo = "Ciencias",
                    descripcion = "¡Explora el mundo!",
                    color = Verde,
                    modifier = Modifier.weight(1f)
                ) {
                    asignaturaSeleccionada = "Ciencias Naturales"
                }

                TarjetaAsignatura(
                    emoji = "🌎",
                    titulo = "Historia",
                    descripcion = "¡Conoce tu mundo!",
                    color = Morado,
                    modifier = Modifier.weight(1f)
                ) {
                    asignaturaSeleccionada = "Historia, Geografía y Ciencias Sociales"
                }
            }

            Spacer(Modifier.height(24.dp))

            TarjetaProgreso()

            Spacer(Modifier.height(18.dp))

            if (asignaturaSeleccionada.isNotEmpty()) {
                TarjetaSeleccion(asignaturaSeleccionada) { asignaturaSeleccionada = "" }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Encabezado() {
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
                style = MaterialTheme.typography.headlineMedium,
                color = Azul
            )
            Text(
                text = "¡Aprender es una aventura!",
                style = MaterialTheme.typography.bodyMedium,
                color = TintaSuave
            )
        }
    }
}

@Composable
private fun TarjetaSaludo() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Azul),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¡Hola, Martín! 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Blanco
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "¿Qué aprenderemos hoy?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Blanco
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "¡Vamos a jugar y aprender!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Blanco
                )
            }

            Text("🧒", fontSize = 60.sp)
        }
    }
}

@Composable
private fun TarjetaProgreso() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Blanco),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 27.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "¡Mira cuánto has avanzado!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Tinta
                    )
                    Text(
                        text = "3 de 5 actividades completadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TintaSuave
                    )
                }
            }

            Spacer(Modifier.height(15.dp))

            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = Verde,
                trackColor = VerdeClaro,
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "¡Vas genial! 🌟 60%",
                style = MaterialTheme.typography.labelLarge,
                color = VerdeOscuro
            )
        }
    }
}

@Composable
private fun TarjetaSeleccion(asignatura: String, onCerrar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AzulSuave),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "¡Vamos con $asignatura! 🎉",
                style = MaterialTheme.typography.titleMedium,
                color = Azul
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Pronto podrás explorar sus unidades y actividades.",
                style = MaterialTheme.typography.bodyMedium,
                color = Tinta
            )
            TextButton(onClick = onCerrar) {
                Text("Volver", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF9F0, heightDp = 900)
@Composable
private fun PreviewPantallaInicio() {
    Mpazpro1Theme(darkTheme = false) {
        PantallaInicio()
    }
}
