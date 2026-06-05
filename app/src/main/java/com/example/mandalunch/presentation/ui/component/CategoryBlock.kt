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
import androidx.compose.ui.unit.dp
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.presentation.ui.theme.AccentBlue
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextPrimary

private val BlockBorder = Color.White.copy(alpha = 0.18f)

/**
 * 3x3 카테고리 블록.
 * 중앙 셀에 카테고리 이모지+이름이 표시되고, 나머지 8개 셀에 메뉴 이름이 보인다.
 * 메뉴 목록이 8개 미만이어도 안전하게 placeholder가 들어간다.
 */
@Composable
fun CategoryBlock(
    category: Category,
    menuNames: List<String>,
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
        // row 0: menu0, menu1, menu2
        BlockRow(
            cells = listOf(
                Cell.Menu(safeMenus[0]),
                Cell.Menu(safeMenus[1]),
                Cell.Menu(safeMenus[2])
            )
        )
        // row 1: menu3, center(category), menu4
        BlockRow(
            cells = listOf(
                Cell.Menu(safeMenus[3]),
                Cell.Center("${category.emoji}\n${category.name}"),
                Cell.Menu(safeMenus[4])
            )
        )
        // row 2: menu5, menu6, menu7
        BlockRow(
            cells = listOf(
                Cell.Menu(safeMenus[5]),
                Cell.Menu(safeMenus[6]),
                Cell.Menu(safeMenus[7])
            )
        )
    }
}

private sealed class Cell {
    data class Menu(val text: String) : Cell()
    data class Center(val text: String) : Cell()
}

@Composable
private fun BlockRow(cells: List<Cell>) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        cells.forEach { cell ->
            when (cell) {
                is Cell.Menu -> GridCell(
                    text = cell.text,
                    isCenter = false,
                    backgroundColor = SurfaceDark,
                    textColor = TextPrimary
                )
                is Cell.Center -> GridCell(
                    text = cell.text,
                    isCenter = true,
                    backgroundColor = Surface2Dark
                )
            }
        }
    }
}
