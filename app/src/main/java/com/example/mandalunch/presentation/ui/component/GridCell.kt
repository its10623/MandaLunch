package com.example.mandalunch.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextPrimary

private val DefaultBorder = Color.White.copy(alpha = 0.18f)

@Composable
fun GridCell(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 38,
    isCenter: Boolean = false,
    backgroundColor: Color = SurfaceDark,
    textColor: Color = TextPrimary,
    isHighlighted: Boolean = false,
    highlightColor: Color = Color.Transparent,
    fontSize: Int = if (isCenter) 11 else 9
) {
    val bg = if (isHighlighted) highlightColor else if (isCenter) Surface2Dark else backgroundColor
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(
                width = if (isHighlighted) 1.5.dp else 0.5.dp,
                color = if (isHighlighted) highlightColor else DefaultBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            style = TextStyle(
                fontSize = fontSize.sp,
                fontWeight = if (isCenter) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}
