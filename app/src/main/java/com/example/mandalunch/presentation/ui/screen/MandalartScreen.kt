package com.example.mandalunch.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
    viewModel: MandalartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.resetSpin()
        viewModel.events.collectLatest { ev ->
            when (ev) {
                is MandalartUiEvent.NavigateToMenuSelect -> onNavigateToMenuSelect(ev.categoryId)
                is MandalartUiEvent.NavigateToMenuEdit -> onNavigateToMenuEdit(ev.categoryId)
            }
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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header (좌: 타이틀 / 우: 즐겨찾기, 히스토리 버튼)
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            // 9x9 만다라트 그리드
            Mandala9x9Grid(
                categories = state.categories,
                menusByCategoryId = state.menusByCategoryId,
                spinState = state.spinState,
                highlightedCwIndex = state.highlightedIndex,
                onSpinClick = viewModel::onSpinClick,
                onCategoryClick = viewModel::onCategoryClick
            )

            Spacer(Modifier.height(32.dp))

            // 하단 두 상태 버튼
            when (val s = state.spinState) {
                is SpinState.Selected -> {
                    val cat = MandalaLayout.cwIndexToCategory(s.categoryIndex, state.categories)
                    GradientButton(
                        text = "🍽️ ${cat?.name ?: ""} 메뉴 보기",
                        gradient = listOf(AccentOrange, AccentOrange.copy(alpha = 0.85f)),
                        onClick = viewModel::onGoToMenuClick
                    )
                }
                else -> {
                    GradientButton(
                        text = "🎲 랜덤 뽑기",
                        gradient = listOf(AccentRed, AccentOrange),
                        enabled = s !is SpinState.Spinning,
                        onClick = viewModel::onSpinClick
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
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
    // position 별 카테고리 매핑
    val byPos = categories.associateBy { it.position }

    // 현재 선택/하이라이트된 DB position 산출 (외곽 블록 강조용)
    val highlightedPosition: Int? = when {
        spinState is SpinState.Selected ->
            MandalaLayout.CW_TO_POSITION.getOrNull(spinState.categoryIndex)
        spinState is SpinState.Spinning && highlightedCwIndex in 0..7 ->
            MandalaLayout.CW_TO_POSITION[highlightedCwIndex]
        else -> null
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 블록 행 0: position 0, 1, 2
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BlockAt(byPos[0], menusByCategoryId, highlightedPosition, onCategoryClick)
            BlockAt(byPos[1], menusByCategoryId, highlightedPosition, onCategoryClick)
            BlockAt(byPos[2], menusByCategoryId, highlightedPosition, onCategoryClick)
        }
        // 블록 행 1: position 3, SPIN(center), 4
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BlockAt(byPos[3], menusByCategoryId, highlightedPosition, onCategoryClick)
            SpinBlock(
                categories = categories,
                spinState = spinState,
                highlightedCwIndex = highlightedCwIndex,
                onSpinClick = onSpinClick
            )
            BlockAt(byPos[4], menusByCategoryId, highlightedPosition, onCategoryClick)
        }
        // 블록 행 2: position 5, 6, 7
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BlockAt(byPos[5], menusByCategoryId, highlightedPosition, onCategoryClick)
            BlockAt(byPos[6], menusByCategoryId, highlightedPosition, onCategoryClick)
            BlockAt(byPos[7], menusByCategoryId, highlightedPosition, onCategoryClick)
        }
    }
}

@Composable
private fun BlockAt(
    category: Category?,
    menusByCategoryId: Map<Int, List<Menu>>,
    highlightedPosition: Int?,
    onCategoryClick: (Category) -> Unit
) {
    if (category == null) {
        // 데이터 로딩 전 placeholder
        Column {
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        GridCell(text = "")
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
            onClick = { onCategoryClick(category) },
            isHighlighted = highlightedPosition == category.position
        )
    }
}

