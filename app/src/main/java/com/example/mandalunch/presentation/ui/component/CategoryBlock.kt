package com.example.mandalunch.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.presentation.ui.theme.AccentBlue
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextPrimary

private val BlockBorder = Color.White.copy(alpha = 0.18f)

@Composable
fun CategoryBlock(
    category: Category,
    menuNames: List<String>,
    cellSize: Dp = 38.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isHighlighted: Boolean = false
) {
    val safeMenus = (menuNames + List(8) { "" }).take(8)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceDark)
            .border(
                width = if (isHighlighted) 2.dp else 1.dp,
                color = if (isHighlighted) AccentBlue else BlockBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        BlockRow(
            cells = listOf(
                Cell.Menu(safeMenus[0]),
                Cell.Menu(safeMenus[1]),
                Cell.Menu(safeMenus[2])
            ),
            cellSize = cellSize
        )
        BlockRow(
            cells = listOf(
                Cell.Menu(safeMenus[3]),
                Cell.Center("${category.emoji}\n${category.name}"),
                Cell.Menu(safeMenus[4])
            ),
            cellSize = cellSize
        )
        BlockRow(
            cells = listOf(
                Cell.Menu(safeMenus[5]),
                Cell.Menu(safeMenus[6]),
                Cell.Menu(safeMenus[7])
            ),
            cellSize = cellSize
        )
    }
}

private sealed class Cell {
    data class Menu(val text: String) : Cell()
    data class Center(val text: String) : Cell()
}

@Composable
private fun BlockRow(cells: List<Cell>, cellSize: Dp) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        cells.forEach { cell ->
            when (cell) {
                is Cell.Menu -> GridCell(
                    text = cell.text,
                    size = cellSize,
                    isCenter = false,
                    backgroundColor = SurfaceDark,
                    textColor = TextPrimary
                )
                is Cell.Center -> GridCell(
                    text = cell.text,
                    size = cellSize,
                    isCenter = true,
                    backgroundColor = Surface2Dark
                )
            }
        }
    }
}
