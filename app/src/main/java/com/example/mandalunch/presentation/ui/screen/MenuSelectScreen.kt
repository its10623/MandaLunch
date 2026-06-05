package com.example.mandalunch.presentation.ui.screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.mandalunch.presentation.ui.component.GradientButton
import com.example.mandalunch.presentation.ui.theme.AccentBlue
import com.example.mandalunch.presentation.ui.theme.AccentGreen
import com.example.mandalunch.presentation.ui.theme.AccentOrange
import com.example.mandalunch.presentation.ui.theme.AccentRed
import com.example.mandalunch.presentation.ui.theme.BackgroundDark
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextDim
import com.example.mandalunch.presentation.ui.theme.TextPrimary
import com.example.mandalunch.presentation.viewmodel.MenuSelectUiEvent
import com.example.mandalunch.presentation.viewmodel.MenuSelectViewModel
import com.example.mandalunch.presentation.viewmodel.MenuSpinState
import kotlinx.coroutines.flow.collectLatest

private val CellBorder = Color.White.copy(alpha = 0.18f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSelectScreen(
    onBack: () -> Unit,
    onNavigateToResult: (Int, String) -> Unit,
    viewModel: MenuSelectViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val category: Category? = state.category
    val menus: List<Menu> = state.menus

    LaunchedEffect(Unit) {
        viewModel.resetSpin()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { ev ->
            when (ev) {
                is MenuSelectUiEvent.NavigateToResult ->
                    onNavigateToResult(ev.categoryId, ev.menuName)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopAppBar(
            title = {
                if (category != null) {
                    Text(
                        text = "${category.emoji} ${category.name}",
                        color = TextPrimary,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    )
                } else {
                    Text(text = "", color = TextPrimary)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = TextPrimary
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "메뉴를 골라보세요",
                color = TextDim,
                style = TextStyle(fontSize = 13.sp)
            )
            Spacer(Modifier.height(20.dp))

            MenuGrid3x3(
                category = category,
                menus = menus,
                spinState = state.spinState,
                highlightedIndex = state.highlightedIndex,
                onToggleFavorite = viewModel::onToggleFavorite
            )

            Spacer(Modifier.height(20.dp))

            when (val s = state.spinState) {
                is MenuSpinState.Selected -> {
                    GradientButton(
                        text = "🎉 결과 보기",
                        gradient = listOf(AccentOrange, AccentOrange.copy(alpha = 0.85f)),
                        onClick = viewModel::onGoToResult
                    )
                }
                else -> {
                    GradientButton(
                        text = "🎲 메뉴 뽑기",
                        gradient = listOf(AccentRed, AccentOrange),
                        enabled = s !is MenuSpinState.Spinning,
                        onClick = viewModel::onSpinClick
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MenuGrid3x3(
    category: Category?,
    menus: List<Menu>,
    spinState: MenuSpinState,
    highlightedIndex: Int,
    onToggleFavorite: (Menu) -> Unit
) {
    // 8개 메뉴 슬롯. 시계방향 배치 (PRD v2 §4-2):
    // Row0: [0],[1],[2]
    // Row1: [7], [카테고리], [3]
    // Row2: [6],[5],[4]
    val safeMenus = (menus + List(8) { null }).take(8)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuCell(menuIndex = 0, menu = safeMenus[0], spinState = spinState, highlightedIndex = highlightedIndex, onToggleFavorite = onToggleFavorite)
            MenuCell(menuIndex = 1, menu = safeMenus[1], spinState = spinState, highlightedIndex = highlightedIndex, onToggleFavorite = onToggleFavorite)
            MenuCell(menuIndex = 2, menu = safeMenus[2], spinState = spinState, highlightedIndex = highlightedIndex, onToggleFavorite = onToggleFavorite)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuCell(menuIndex = 7, menu = safeMenus[7], spinState = spinState, highlightedIndex = highlightedIndex, onToggleFavorite = onToggleFavorite)
            CategoryCenterCell(category = category)
            MenuCell(menuIndex = 3, menu = safeMenus[3], spinState = spinState, highlightedIndex = highlightedIndex, onToggleFavorite = onToggleFavorite)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MenuCell(menuIndex = 6, menu = safeMenus[6], spinState = spinState, highlightedIndex = highlightedIndex, onToggleFavorite = onToggleFavorite)
            MenuCell(menuIndex = 5, menu = safeMenus[5], spinState = spinState, highlightedIndex = highlightedIndex, onToggleFavorite = onToggleFavorite)
            MenuCell(menuIndex = 4, menu = safeMenus[4], spinState = spinState, highlightedIndex = highlightedIndex, onToggleFavorite = onToggleFavorite)
        }
    }
}

@Composable
private fun MenuCell(
    menuIndex: Int,
    menu: Menu?,
    spinState: MenuSpinState,
    highlightedIndex: Int,
    onToggleFavorite: (Menu) -> Unit
) {
    val isGlow = spinState is MenuSpinState.Spinning && highlightedIndex == menuIndex
    val isSelected = spinState is MenuSpinState.Selected &&
        spinState.menuIndex == menuIndex

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(isSelected) {
        if (isSelected) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "menuCellScale"
    )

    val brush: Brush = when {
        isSelected -> Brush.horizontalGradient(listOf(AccentRed, AccentOrange))
        isGlow     -> Brush.horizontalGradient(listOf(AccentGreen, AccentBlue))
        else       -> Brush.horizontalGradient(listOf(SurfaceDark, SurfaceDark))
    }
    val textColor: Color = if (isGlow || isSelected) TextPrimary else TextPrimary
    val fontWeight = if (isGlow || isSelected) FontWeight.Bold else FontWeight.Medium
    val borderColor: Color = when {
        isSelected -> AccentRed
        isGlow     -> AccentGreen
        else       -> CellBorder
    }
    val borderWidth = if (isGlow || isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(110.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(brush)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(
            text = menu?.name ?: "",
            color = textColor,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = fontWeight
            ),
            maxLines = 2,
            modifier = Modifier.align(Alignment.Center)
        )
        // 우상단 하트 토글: 스핀 중(Spinning/Selected)에는 노출하지 않음
        if (menu != null && spinState is MenuSpinState.Idle) {
            Icon(
                imageVector = if (menu.isFavorite) Icons.Filled.Favorite
                              else Icons.Filled.FavoriteBorder,
                contentDescription = if (menu.isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                tint = if (menu.isFavorite) AccentRed else TextDim,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clickable { onToggleFavorite(menu) }
            )
        }
    }
}

@Composable
private fun CategoryCenterCell(category: Category?) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2Dark)
            .border(width = 1.5.dp, color = AccentBlue, shape = RoundedCornerShape(10.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category?.emoji ?: "",
                color = TextPrimary,
                style = TextStyle(fontSize = 28.sp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = category?.name ?: "",
                color = TextPrimary,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )
        }
    }
}
