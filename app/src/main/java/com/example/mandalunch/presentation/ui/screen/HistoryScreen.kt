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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.mandalunch.presentation.ui.theme.AccentOrange
import com.example.mandalunch.presentation.ui.theme.AccentRed
import com.example.mandalunch.presentation.ui.theme.BackgroundDark
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextDim
import com.example.mandalunch.presentation.ui.theme.TextPrimary
import com.example.mandalunch.presentation.viewmodel.HistoryItemUi
import com.example.mandalunch.presentation.viewmodel.HistorySection
import com.example.mandalunch.presentation.viewmodel.HistoryUiEvent
import com.example.mandalunch.presentation.viewmodel.HistoryViewModel
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                HistoryUiEvent.NavigateBack -> onBack()
                HistoryUiEvent.DeleteAllCompleted -> { /* Snackbar 등 추후 확장 */ }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "추천 히스토리",
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
                    Text(
                        text = "전체 삭제",
                        color = AccentRed,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .clickable { viewModel.onDeleteAllClick() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
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
                        text = "아직 추천 기록이 없어요 🍽️",
                        color = TextDim,
                        style = TextStyle(fontSize = 15.sp),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    HistoryList(sections = state.sections)
                }
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteConfirm,
            containerColor = Surface2Dark,
            title = {
                Text(
                    text = "전체 삭제",
                    color = TextPrimary,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Text(
                    text = "모든 추천 기록을 삭제할까요?\n이 작업은 되돌릴 수 없어요.",
                    color = TextDim,
                    style = TextStyle(fontSize = 14.sp)
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmDeleteAll) {
                    Text(
                        text = "삭제",
                        color = AccentRed,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteConfirm) {
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
private fun HistoryList(sections: List<HistorySection>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sections.forEach { section ->
            item(key = "header_${section.group.name}") {
                SectionHeader(label = section.group.label)
            }
            items(
                items = section.items,
                key = { it.id }
            ) { item ->
                HistoryRow(item = item)
            }
            item(key = "spacer_${section.group.name}") {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        color = TextDim,
        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
private fun HistoryRow(item: HistoryItemUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2Dark, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.categoryEmoji,
                style = TextStyle(fontSize = 22.sp)
            )
            Spacer(Modifier.padding(horizontal = 6.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.menuName,
                    color = TextPrimary,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                if (item.categoryName.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.categoryName,
                        color = AccentOrange,
                        style = TextStyle(fontSize = 12.sp)
                    )
                }
            }
            Text(
                text = item.timeLabel,
                color = TextDim,
                style = TextStyle(fontSize = 12.sp)
            )
        }
    }
}
