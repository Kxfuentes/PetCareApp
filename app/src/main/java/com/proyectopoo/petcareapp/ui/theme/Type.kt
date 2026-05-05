package com.proyectopoo.petcareapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp

val Typography = Typography(

    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),

    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),

    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),

    bodyLarge = TextStyle(
        fontSize = 16.sp
    ),

    bodyMedium = TextStyle(
        fontSize = 14.sp
    ),

    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)