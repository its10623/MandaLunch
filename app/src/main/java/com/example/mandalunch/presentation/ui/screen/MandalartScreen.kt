package com.example.mandalunch.presentation.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mandalunch.R
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.presentation.ui.component.CategoryBlock
import com.example.mandalunch.presentation.ui.component.GradientButton
import com.example.mandalunch.presentation.ui.component.GridCell
import com.example.mandalunch.presentation.ui.component.SpinBlock
import com.example.mandalunch.presentation.ui.theme.AccentOrange
import com.example.mandalunch.presentation.ui.theme.AccentRed
import com.example.mandalunch.presentation.ui.theme.BackgroundDark
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextDim
import com.example.mandalunch.presentation.ui.theme.TextPrimary
import com.example.mandalunch.presentation.viewmodel.MandalartUiEvent
import com.example.mandalunch.presentation.viewmodel.MandalartViewModel
import com.example.mandalunch.presentation.viewmodel.SpinState
import com.example.mandalunch.presentation.viewmodel.util.MandalaLayout
import kotlinx.coroutines.flow.collectLatest

/**
 * 9x9 만다라트 그리드 화면.
 * 9개의 3x3 블록을 3x3으로 배치한다.
 * 가운데 블록은 SpinBlock(CENTER), 나머지 8개는 외곽 CategoryBlock.
 */
@Composable
fun MandalartScreen(
    onNavigateToMenuSelect: (Int) -> Unit,
    onNavigateToMenuEdit: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    autoSpin: Boolean = false,
    onAutoSpinConsumed: () -> Unit = {},
    viewModel: MandalartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showTutorial by remember {
        val prefs = context.getSharedPreferences("mandalunch_prefs", Context.MODE_PRIVATE)
        mutableStateOf(!prefs.getBoolean("tutorial_seen", false))
    }

    LaunchedEffect(Unit) {
        viewModel.resetSpin()
        viewModel.events.collectLatest { ev ->
            when (ev) {
                is MandalartUiEvent.NavigateToMenuSelect -> onNavigateToMenuSelect(ev.categoryId)
                is MandalartUiEvent.NavigateToMenuEdit -> onNavigateToMenuEdit(ev.categoryId)
            }
        }
    }

    LaunchedEffect(autoSpin) {
        if (autoSpin) {
            onAutoSpinConsumed()
            viewModel.resetSpin()
            viewModel.onSpinClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header (좌: 타이틀 / 우: 즐겨찾기, 히스토리 버튼)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MandaLunch",
                        color = TextPrimary,
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.title_mandalart),
                        color = TextDim,
                        style = TextStyle(fontSize = 13.sp)
                    )
                }
                Text(
                    text = "♥",
                    color = AccentRed,
                    style = TextStyle(fontSize = 24.sp),
                    modifier = Modifier
                        .clickable { onNavigateToFavorites() }
                        .padding(8.dp)
                )
                Text(
                    text = "🕐",
                    style = TextStyle(fontSize = 24.sp),
                    modifier = Modifier
                        .clickable { onNavigateToHistory() }
                        .padding(8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // 9x9 만다라트 그리드 — 좌우 4dp만 남겨 최대한 넓게 사용
            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                Mandala9x9Grid(
                    categories = state.categories,
                    menusByCategoryId = state.menusByCategoryId,
                    spinState = state.spinState,
                    highlightedCwIndex = state.highlightedIndex,
                    onSpinClick = viewModel::onSpinClick,
                    onCategoryClick = viewModel::onCategoryClick
                )
            }

            Spacer(Modifier.height(32.dp))

            // 하단 두 상태 버튼
        when (val s = state.spinState) {
                is SpinState.Selected -> {
                    val cat = MandalaLayout.cwIndexToCategory(s.categoryIndex, state.categories)
                    Box(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                        GradientButton(
                            text = "🍽️ ${cat?.name ?: ""} 메뉴 보기",
                            gradient = listOf(AccentOrange, AccentOrange.copy(alpha = 0.85f)),
                            onClick = viewModel::onGoToMenuClick
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                        GradientButton(
                            text = "🎲 랜덤 뽑기",
                            gradient = listOf(AccentRed, AccentOrange),
                            enabled = s !is SpinState.Spinning,
                            onClick = viewModel::onSpinClick
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        if (showTutorial) {
            TutorialOverlay(onDismiss = {
                context.getSharedPreferences("mandalunch_prefs", Context.MODE_PRIVATE)
                    .edit().putBoolean("tutorial_seen", true).apply()
                showTutorial = false
            })
        }
    }
}

@Composable
private fun Mandala9x9Grid(
    categories: List<Category>,
    menusByCategoryId: Map<Int, List<Menu>>,
    spinState: SpinState,
    highlightedCwIndex: Int,
    onSpinClick: () -> Unit,
    onCategoryClick: (Category) -> Unit
) {
    val byPos = categories.associateBy { it.position }

    val highlightedPosition: Int? = when {
        spinState is SpinState.Selected ->
            MandalaLayout.CW_TO_POSITION.getOrNull(spinState.categoryIndex)
        spinState is SpinState.Spinning && highlightedCwIndex in 0..7 ->
            MandalaLayout.CW_TO_POSITION[highlightedCwIndex]
        else -> null
    }

    // 화면 너비에서 셀 크기를 동적으로 계산한다.
    // 9셀 × cellSize + 간격(블록 사이 4dp×2 + 셀 사이 2dp×2×3 + 블록 내부 패딩 2dp×2×3) = 32dp
    BoxWithConstraints {
        val cellSize = ((maxWidth - 32.dp) / 9).coerceIn(28.dp, 52.dp)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BlockAt(byPos[0], menusByCategoryId, highlightedPosition, cellSize, onCategoryClick)
                BlockAt(byPos[1], menusByCategoryId, highlightedPosition, cellSize, onCategoryClick)
                BlockAt(byPos[2], menusByCategoryId, highlightedPosition, cellSize, onCategoryClick)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BlockAt(byPos[3], menusByCategoryId, highlightedPosition, cellSize, onCategoryClick)
                SpinBlock(
                    categories = categories,
                    spinState = spinState,
                    highlightedCwIndex = highlightedCwIndex,
                    onSpinClick = onSpinClick,
                    cellSize = cellSize
                )
                BlockAt(byPos[4], menusByCategoryId, highlightedPosition, cellSize, onCategoryClick)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BlockAt(byPos[5], menusByCategoryId, highlightedPosition, cellSize, onCategoryClick)
                BlockAt(byPos[6], menusByCategoryId, highlightedPosition, cellSize, onCategoryClick)
                BlockAt(byPos[7], menusByCategoryId, highlightedPosition, cellSize, onCategoryClick)
            }
        }
    }
}

@Composable
private fun TutorialOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "MandaLunch 사용법",
                color = TextPrimary,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            val tips = listOf(
                "✏️" to "카테고리를 탭하면 메뉴를 편집할 수 있어요",
                "🎲" to "중앙 버튼으로 오늘의 카테고리를 랜덤 선택해요",
                "🍽️" to "선택된 카테고리에서 메뉴를 골라요",
                "🔄" to "앱을 켤 때마다 메뉴가 새롭게 배치돼요"
            )
            tips.forEach { (emoji, desc) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = emoji, style = TextStyle(fontSize = 22.sp))
                    Text(
                        text = desc,
                        color = TextPrimary,
                        style = TextStyle(fontSize = 14.sp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Surface2Dark)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "시작하기!",
                    color = AccentOrange,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun BlockAt(
    category: Category?,
    menusByCategoryId: Map<Int, List<Menu>>,
    highlightedPosition: Int?,
    cellSize: androidx.compose.ui.unit.Dp,
    onCategoryClick: (Category) -> Unit
) {
    if (category == null) {
        Column {
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        GridCell(text = "", size = cellSize)
                    }
                }
                Spacer(Modifier.height(2.dp))
            }
        }
    } else {
        val menuNames = menusByCategoryId[category.id]?.map { it.name } ?: emptyList()
        CategoryBlock(
            category = category,
            menuNames = menuNames,
            cellSize = cellSize,
            onClick = { onCategoryClick(category) },
            isHighlighted = highlightedPosition == category.position
        )
    }
}

