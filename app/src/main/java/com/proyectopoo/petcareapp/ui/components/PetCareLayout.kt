package com.proyectopoo.petcareapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.proyectopoo.petcareapp.ui.theme.CafeMedio
import com.proyectopoo.petcareapp.ui.theme.CafeOscuro
import com.proyectopoo.petcareapp.ui.theme.FondoClaro
import com.proyectopoo.petcareapp.ui.theme.FondoCrema

@Composable
fun PantallaPetCare(contenido: @Composable ColumnScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(FondoCrema, FondoClaro)))) {

        Box(modifier = Modifier.size(180.dp).offset((-60).dp, (-60).dp).clip(CircleShape).background(CafeMedio))
        Box(modifier = Modifier.size(220.dp).align(Alignment.TopEnd).offset(60.dp, (-70).dp).clip(CircleShape).background(CafeOscuro))
        Box(modifier = Modifier.size(150.dp).align(Alignment.BottomStart).offset((-70).dp, 70.dp).clip(CircleShape).background(CafeOscuro))

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = contenido
        )
    }
}