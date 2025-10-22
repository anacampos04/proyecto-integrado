package com.example.anacampospi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
// Si se añade fuentes a /res/font, referencia aquí (ej. poppins_*.ttf)
val Poppins = FontFamily.Default // cambiarlo por FontFamily(Font(R.font.poppins_regular), ...)

val Typography = Typography(
    titleLarge = TextStyle(fontFamily = Poppins, fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = Poppins, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = Poppins, fontSize = 16.sp),
    labelLarge = TextStyle(fontFamily = Poppins, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
)
