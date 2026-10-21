package com.example.mpaz_pro_1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mpaz_pro_1.ui.theme.Morado
import com.example.mpaz_pro_1.ui.theme.Rosado
import com.example.mpaz_pro_1.ui.theme.Tinta

/**
 * Tarjeta de asignatura.
 *
 * - Usa el overload `Card(onClick = ...)`: aporta rol de botón para TalkBack y
 *   recorta el ripple a la `shape` (con `Modifier.clickable` el ripple salía cuadrado).
 * - `heightIn(min = ...)` en vez de `height`: si el usuario sube el tamaño de
 *   letra, la tarjeta crece en vez de recortar el texto.
 * - El emoji es decorativo (el título ya dice la asignatura) así que se oculta
 *   de los lectores de pantalla.
 */
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
        onClick = onClick,
        modifier = modifier.heightIn(min = 168.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 30.sp,
                modifier = Modifier.clearAndSetSemantics { }
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = Tinta,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = Tinta,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF9F0, widthDp = 200)
@Composable
private fun PreviewTarjeta() {
    TarjetaAsignatura(
        emoji = "📚",
        titulo = "Lenguaje",
        descripcion = "¡Lee y descubre!",
        color = Rosado,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF9F0, widthDp = 200)
@Composable
private fun PreviewTarjetaLarga() {
    TarjetaAsignatura(
        emoji = "🌎",
        titulo = "Historia, Geografía y Ciencias Sociales",
        descripcion = "¡Conoce tu mundo!",
        color = Morado,
        onClick = {}
    )
}
