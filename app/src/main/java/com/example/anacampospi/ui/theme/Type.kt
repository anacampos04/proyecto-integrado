package com.example.anacampospi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.anacampospi.R

// Poppins - Tipografía principal moderna y elegante
val PoppinsFamily = FontFamily(
    Font(R.font.poppinsregular, FontWeight.Normal),
    Font(R.font.poppinsmedium, FontWeight.Medium),
    Font(R.font.poppinssemibold, FontWeight.SemiBold),
    Font(R.font.poppinsbold, FontWeight.Bold)
)

val Typography = Typography(
    // Títulos grandes - para pantallas principales (POPPINS)
    displayLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 57.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 45.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),

    // Headlines - para secciones (POPPINS)
    headlineLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),

    // Títulos - para cards y componentes (POPPINS)
    titleLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),

    // Body - para contenido general (POPPINS Regular - legibilidad óptima)
    bodyLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp
    ),

    // Labels - para botones y etiquetas (POPPINS Medium)
    labelLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PoppinsFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)
