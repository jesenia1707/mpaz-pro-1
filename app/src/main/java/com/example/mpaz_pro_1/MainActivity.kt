package com.example.mpaz_pro_1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mpaz_pro_1.ui.screens.PantallaInicio
import com.example.mpaz_pro_1.ui.theme.Mpazpro1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Mpazpro1Theme {
                PantallaInicio()
            }
        }
    }
}
