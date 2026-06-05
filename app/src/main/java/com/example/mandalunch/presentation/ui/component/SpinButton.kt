package com.example.mandalunch.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mandalunch.presentation.ui.theme.AccentOrange
import com.example.mandalunch.presentation.ui.theme.TextPrimary

/**
 * 일반 버튼 (단색 배경).
 */
@Composable
fun SpinButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = AccentOrange,
    contentColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        )
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

/**
 * 그라디언트 배경 버튼. PRD v2 SPIN 버튼 스타일.
 */
@Composable
fun GradientButton(
    text: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    val brush = if (enabled) {
        Brush.horizontalGradient(gradient)
    } else {
        Brush.horizontalGradient(gradient.map { it.copy(alpha = 0.5f) })
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.7f),
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
