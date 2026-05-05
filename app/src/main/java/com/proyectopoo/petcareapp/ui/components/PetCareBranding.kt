package com.proyectopoo.petcareapp.ui.components


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import com.proyectopoo.petcareapp.ui.theme.CafeMedio

@Composable
fun TituloPetCare() {
    Text(
        text = "PetCare",
        fontSize = 52.sp,
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        color = CafeMedio
    )
}