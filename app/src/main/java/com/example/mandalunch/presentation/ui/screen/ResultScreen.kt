package com.example.mandalunch.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mandalunch.presentation.share.KakaoShareLauncher
import com.example.mandalunch.presentation.ui.component.GradientButton
import com.example.mandalunch.presentation.ui.component.SpinButton
import com.example.mandalunch.presentation.ui.theme.AccentBlue
import com.example.mandalunch.presentation.ui.theme.AccentOrange
import com.example.mandalunch.presentation.ui.theme.AccentRed
import com.example.mandalunch.presentation.ui.theme.BackgroundDark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextDim
import com.example.mandalunch.presentation.ui.theme.TextPrimary
import com.example.mandalunch.presentation.util.findActivity
import com.example.mandalunch.presentation.viewmodel.ResultUiEvent
import com.example.mandalunch.presentation.viewmodel.ResultViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ResultScreen(
    onNavigateBackToMandalart: () -> Unit,
    onNavigateBackToMenuSelect: () -> Unit,
    onNavigateToRestaurant: (String, String) -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { ev ->
            when (ev) {
                is ResultUiEvent.NavigateBackToMandalart -> onNavigateBackToMandalart()
                is ResultUiEvent.NavigateBackToMenuSelect -> onNavigateBackToMenuSelect()
                is ResultUiEvent.NavigateToRestaurant -> onNavigateToRestaurant(ev.menuName, ev.categoryName)
                is ResultUiEvent.ShareToKakao -> KakaoShareLauncher.share(context.findActivity(), ev.message)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // 공유 버튼 (우상단 오버레이)
        IconButton(
            onClick = viewModel::onShareClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 4.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "카카오톡으로 공유",
                tint = TextDim
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 카테고리 이모지
            Text(
                text = state.category?.emoji ?: "",
                style = TextStyle(fontSize = 72.sp),
                color = TextPrimary
            )

            Spacer(Modifier.height(20.dp))

            // 카테고리 배지 (빨강 pill)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentRed)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = state.category?.name ?: "",
                    color = TextPrimary,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // 메뉴 이름 (그라디언트 텍스트)
            val gradientBrush = Brush.linearGradient(
                colors = listOf(AccentOrange, AccentRed)
            )
            Text(
                text = state.menuName,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    brush = gradientBrush
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "오늘의 점심은 이걸로 정해졌습니다! 🎉",
                color = TextDim,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 14.sp)
            )

            Spacer(Modifier.height(48.dp))

            GradientButton(
                text = "🔄 처음부터 다시",
                gradient = listOf(AccentRed, AccentOrange),
                onClick = viewModel::onRestartClick
            )

            Spacer(Modifier.height(12.dp))

            SpinButton(
                text = "🔀 같은 카테고리에서 다시",
                backgroundColor = SurfaceDark,
                contentColor = TextPrimary,
                onClick = viewModel::onSameCategoryAgainClick
            )

            Spacer(Modifier.height(12.dp))

            SpinButton(
                text = "🗺️ 근처 음식점 찾기",
                backgroundColor = AccentBlue,
                contentColor = TextPrimary,
                onClick = viewModel::onFindRestaurantsClick
            )
        }
    }
}
