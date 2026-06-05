package com.example.mandalunch.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.presentation.ui.theme.AccentOrange
import com.example.mandalunch.presentation.ui.theme.AccentRed
import com.example.mandalunch.presentation.ui.theme.BackgroundDark
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextDim
import com.example.mandalunch.presentation.ui.theme.TextPrimary
import com.example.mandalunch.presentation.viewmodel.FavoriteCategorySection
import com.example.mandalunch.presentation.viewmodel.FavoriteUiEvent
import com.example.mandalunch.presentation.viewmodel.FavoriteViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onBack: () -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                FavoriteUiEvent.NavigateBack -> onBack()
                FavoriteUiEvent.ClearAllCompleted -> { /* Snackbar 등 추후 확장 */ }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "즐겨찾기",
                        color = TextPrimary,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    Text(
                        text = "← 돌아가기",
                        color = TextPrimary,
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier
                            .clickable { viewModel.onBackClick() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                },
                actions = {
                    if (!state.isEmpty) {
                        Text(
                            text = "전체 해제",
                            color = AccentRed,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                            modifier = Modifier
                                .clickable { viewModel.onClearAllClick() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        color = AccentOrange,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.isEmpty -> {
                    Text(
                        text = "아직 즐겨찾기한 메뉴가 없어요 ♥",
                        color = TextDim,
                        style = TextStyle(fontSize = 15.sp),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    FavoriteList(
                        sections = state.sections,
                        onToggleFavorite = viewModel::onToggleFavorite
                    )
                }
            }
        }
    }

    if (state.showClearConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissClearConfirm,
            containerColor = Surface2Dark,
            title = {
                Text(
                    text = "전체 해제",
                    color = TextPrimary,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Text(
                    text = "모든 즐겨찾기를 해제할까요?\n이 작업은 되돌릴 수 없어요.",
                    color = TextDim,
                    style = TextStyle(fontSize = 14.sp)
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmClearAll) {
                    Text(
                        text = "해제",
                        color = AccentRed,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissClearConfirm) {
                    Text(
                        text = "취소",
                        color = TextDim,
                        style = TextStyle(fontSize = 14.sp)
                    )
                }
            }
        )
    }
}

@Composable
private fun FavoriteList(
    sections: List<FavoriteCategorySection>,
    onToggleFavorite: (Menu) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sections.forEach { section ->
            item(key = "header_${section.categoryId}") {
                SectionHeader(
                    emoji = section.categoryEmoji,
                    name = section.categoryName,
                    count = section.menus.size
                )
            }
            items(
                items = section.menus,
                key = { menu -> "fav_${section.categoryId}_${menu.id}" }
            ) { menu ->
                FavoriteRow(
                    menu = menu,
                    onToggleFavorite = onToggleFavorite
                )
            }
            item(key = "spacer_${section.categoryId}") {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(emoji: String, name: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$emoji $name",
            color = TextDim,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${count}개",
            color = TextDim,
            style = TextStyle(fontSize = 11.sp)
        )
    }
}

@Composable
private fun FavoriteRow(
    menu: Menu,
    onToggleFavorite: (Menu) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2Dark, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menu.name,
                    color = TextPrimary,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "즐겨찾기 해제",
                tint = AccentRed,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onToggleFavorite(menu) }
            )
        }
    }
}
