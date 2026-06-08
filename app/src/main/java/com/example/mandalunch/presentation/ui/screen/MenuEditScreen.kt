package com.example.mandalunch.presentation.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.mandalunch.presentation.viewmodel.MenuEditUiEvent
import com.example.mandalunch.presentation.viewmodel.MenuEditViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuEditScreen(
    onBack: () -> Unit,
    viewModel: MenuEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                MenuEditUiEvent.NavigateBack -> onBack()
                is MenuEditUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    val cat = state.category
                    Text(
                        text = if (cat != null) "${cat.emoji} ${cat.name} 메뉴 편집" else "메뉴 편집",
                        color = TextPrimary,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    Text(
                        text = "← 취소",
                        color = TextPrimary,
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier
                            .clickable { viewModel.onCancelBack() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                },
                actions = {
                    val saveEnabled = state.canSave
                    Text(
                        text = "저장",
                        color = if (saveEnabled) AccentGreen else TextDim,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable(enabled = saveEnabled) { viewModel.onSave() }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentOrange)
                    }
                }
                else -> {
                    val canRandomize = (state.boardMenus.size + state.poolMenus.size) >= 8
                        && !state.isSaving
                    MenuEditContent(
                        modifier = Modifier.weight(1f),
                        boardMenus = state.boardMenus,
                        poolMenus = state.poolMenus,
                        canAddToBoard = state.canAddToBoard,
                        canRandomize = canRandomize,
                        pendingMenuName = state.pendingMenuName,
                        onToggleBoardMenu = viewModel::onToggleBoardMenu,
                        onDeleteMenu = viewModel::onDeleteMenu,
                        onUpdatePendingName = viewModel::onUpdatePendingName,
                        onAddMenu = viewModel::onAddMenu,
                        onRandomize = viewModel::onRandomize
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuEditContent(
    modifier: Modifier = Modifier,
    boardMenus: List<Menu>,
    poolMenus: List<Menu>,
    canAddToBoard: Boolean,
    canRandomize: Boolean,
    pendingMenuName: String,
    onToggleBoardMenu: (Menu) -> Unit,
    onDeleteMenu: (Menu) -> Unit,
    onUpdatePendingName: (String) -> Unit,
    onAddMenu: () -> Unit,
    onRandomize: () -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header_board") {
                SectionHeader(
                    title = "보드 메뉴",
                    count = "${boardMenus.size}/8",
                    highlightCount = boardMenus.size != 8
                )
            }
            items(items = boardMenus, key = { "board_${it.id}" }) { menu ->
                MenuEditRow(
                    menu = menu,
                    isOnBoard = true,
                    canAddToBoard = canAddToBoard,
                    onToggle = { onToggleBoardMenu(menu) },
                    onDelete = { }
                )
            }

            item(key = "divider") {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = TextDim.copy(alpha = 0.2f)
                )
            }

            item(key = "header_pool") {
                SectionHeader(
                    title = "메뉴 풀",
                    count = "${poolMenus.size}개",
                    highlightCount = false
                )
            }
            items(items = poolMenus, key = { "pool_${it.id}" }) { menu ->
                MenuEditRow(
                    menu = menu,
                    isOnBoard = false,
                    canAddToBoard = canAddToBoard,
                    onToggle = { onToggleBoardMenu(menu) },
                    onDelete = { onDeleteMenu(menu) }
                )
            }
        }

        // 랜덤 채우기 버튼 — 키보드 올라오면 입력창 바로 위로 함께 올라감
        HorizontalDivider(color = TextDim.copy(alpha = 0.15f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            GradientButton(
                text = "🎲 랜덤 채우기",
                gradient = listOf(AccentRed, AccentOrange),
                enabled = canRandomize,
                onClick = onRandomize
            )
        }

        HorizontalDivider(color = TextDim.copy(alpha = 0.15f))
        AddMenuRow(
            pendingMenuName = pendingMenuName,
            onUpdatePendingName = onUpdatePendingName,
            onAddMenu = onAddMenu,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: String,
    highlightCount: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextPrimary,
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count,
            color = if (highlightCount) AccentRed else TextDim,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun MenuEditRow(
    menu: Menu,
    isOnBoard: Boolean,
    canAddToBoard: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .border(
                width = 1.dp,
                color = if (isOnBoard) AccentBlue.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = menu.name,
            color = TextPrimary,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )

        if (isOnBoard) {
            CircleActionButton(
                text = "✕",
                contentColor = AccentRed,
                backgroundColor = Surface2Dark,
                onClick = onToggle,
                enabled = true
            )
        } else {
            CircleActionButton(
                text = "＋",
                contentColor = if (canAddToBoard) AccentGreen else TextDim,
                backgroundColor = Surface2Dark,
                onClick = onToggle,
                enabled = canAddToBoard
            )
            Spacer(Modifier.padding(start = 6.dp))
            CircleActionButton(
                text = "🗑",
                contentColor = TextPrimary,
                backgroundColor = Surface2Dark,
                onClick = onDelete,
                enabled = true
            )
        }
    }
}

@Composable
private fun CircleActionButton(
    text: String,
    contentColor: Color,
    backgroundColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor.copy(alpha = if (enabled) 1f else 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f),
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun AddMenuRow(
    pendingMenuName: String,
    onUpdatePendingName: (String) -> Unit,
    onAddMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = pendingMenuName,
            onValueChange = onUpdatePendingName,
            placeholder = {
                Text(
                    text = "새 메뉴 이름",
                    color = TextDim,
                    style = TextStyle(fontSize = 14.sp)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (pendingMenuName.isNotBlank()) onAddMenu() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentOrange,
                unfocusedBorderColor = TextDim.copy(alpha = 0.4f),
                cursorColor = AccentOrange,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.padding(start = 8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (pendingMenuName.isNotBlank()) AccentOrange else Surface2Dark
                )
                .clickable(enabled = pendingMenuName.isNotBlank(), onClick = onAddMenu)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "추가",
                color = if (pendingMenuName.isNotBlank()) TextPrimary else TextDim,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}
