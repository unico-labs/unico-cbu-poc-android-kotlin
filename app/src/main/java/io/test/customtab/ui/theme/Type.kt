package io.test.customtab.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.test.customtab.R

val customFontFamily1 = FontFamily(
    Font(R.font.atkinsonhyperlegible_regular)
)
val customFontFamily2 = FontFamily(
    Font(R.font.atkinsonhyperlegible_bold)
)

val MyTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = customFontFamily1,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    
    titleLarge = TextStyle(
        fontFamily = customFontFamily2,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    bodyMedium = TextStyle(
        fontFamily = customFontFamily1,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    
    labelSmall = TextStyle(
        fontFamily = customFontFamily1,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)