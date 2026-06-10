package com.example.mandalunch.presentation.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.presentation.ui.theme.AccentBlue
import com.example.mandalunch.presentation.ui.theme.AccentGreen
import com.example.mandalunch.presentation.ui.theme.AccentOrange
import com.example.mandalunch.presentation.ui.theme.AccentRed
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.TextDim
import com.example.mandalunch.presentation.ui.theme.TextPrimary
import com.example.mandalunch.presentation.viewmodel.SpinState
import com.example.mandalunch.presentation.viewmodel.util.MandalaLayout

/** gridPos 0..8 순서의 방향 이모지. gridPos 4(중심)는 공백. */
private val DIRECTION_EMOJI = arrayOf("↖", "↑", "↗", "←", "", "→", "↙", "↓", "↘")

private val BlockBorder = Color.White.copy(alpha = 0.18f)

@Composable
fun SpinBlock(
    categories: List<Category>,
    spinState: SpinState,
    highlightedCwIndex: Int,
    onSpinClick: () -> Unit,
    cellSize: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Surface2Dark)
            .border(
                width = 1.dp,
                color = BlockBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(3) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) { col ->
                    val gridPos = row * 3 + col
                    if (gridPos == 4) {
                        CenterSpinCell(
                            spinState = spinState,
                            enabled = spinState !is SpinState.Spinning,
                            cellSize = cellSize,
                            onClick = onSpinClick
                        )
                    } else {
                        val cwIdx = MandalaLayout.GRID_TO_CW[gridPos]
                        val targetPos = MandalaLayout.CW_TO_POSITION[cwIdx]
                        val category = categories.firstOrNull { it.position == targetPos }
                        val emoji = DIRECTION_EMOJI[gridPos]
                        val name = category?.name ?: ""
                        val isGlow = spinState is SpinState.Spinning && cwIdx == highlightedCwIndex
                        val isSelected = spinState is SpinState.Selected &&
                            spinState.categoryIndex == cwIdx
                        HintCell(
                            emoji = emoji,
                            name = name,
                            isGlow = isGlow,
                            isSelected = isSelected,
                            cellSize = cellSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HintCell(
    emoji: String,
    name: String,
    isGlow: Boolean,
    isSelected: Boolean,
    cellSize: Dp
) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(isSelected) {
        if (isSelected) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "hintCellScale"
    )

    val brush: Brush = when {
        isSelected -> Brush.horizontalGradient(listOf(AccentRed, AccentOrange))
        isGlow     -> Brush.horizontalGradient(listOf(AccentGreen, AccentBlue))
        else       -> Brush.horizontalGradient(listOf(Surface2Dark, Surface2Dark))
    }

    val textColor: Color = if (isGlow || isSelected) TextPrimary else TextDim
    val fontWeight = if (isGlow || isSelected) FontWeight.Bold else FontWeight.Normal
    val fontSize = (cellSize.value * 0.24f).sp

    Box(
        modifier = Modifier
            .size(cellSize)
            .scale(scale)
            .clip(RoundedCornerShape(4.dp))
            .background(brush)
            .border(
                width = 0.5.dp,
                color = BlockBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (name.isNotEmpty()) "$emoji\n$name" else emoji,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        )
    }
}

@Composable
private fun CenterSpinCell(
    spinState: SpinState,
    enabled: Boolean,
    cellSize: Dp,
    onClick: () -> Unit
) {
    val brush: Brush = when (spinState) {
        is SpinState.Selected ->
            Brush.horizontalGradient(
                listOf(
                    AccentOrange.copy(alpha = 0.5f),
                    AccentOrange.copy(alpha = 0.5f)
                )
            )
        else ->
            Brush.horizontalGradient(listOf(AccentRed, AccentOrange))
    }
    val emoji = when (spinState) {
        is SpinState.Selected -> "🎉"
        else -> "🎲"
    }
    val emojiFontSize = (cellSize.value * 0.47f).sp

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(RoundedCornerShape(4.dp))
            .background(brush)
            .clickable(enabled = enabled) { onClick() }
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = emojiFontSize,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
