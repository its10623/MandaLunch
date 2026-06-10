package com.example.mandalunch.presentation.ui.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.example.mandalunch.domain.model.Restaurant
import com.example.mandalunch.domain.model.SavedLocation
import com.example.mandalunch.presentation.share.KakaoShareLauncher
import com.example.mandalunch.presentation.ui.component.SpinButton
import com.example.mandalunch.presentation.ui.theme.AccentBlue
import com.example.mandalunch.presentation.ui.theme.AccentGreen
import com.example.mandalunch.presentation.ui.theme.AccentOrange
import com.example.mandalunch.presentation.ui.theme.AccentRed
import com.example.mandalunch.presentation.ui.theme.BackgroundDark
import com.example.mandalunch.presentation.ui.theme.Surface2Dark
import com.example.mandalunch.presentation.ui.theme.SurfaceDark
import com.example.mandalunch.presentation.ui.theme.TextDim
import com.example.mandalunch.presentation.ui.theme.TextPrimary
import com.example.mandalunch.presentation.util.cleanCategory
import com.example.mandalunch.presentation.util.findActivity
import com.example.mandalunch.presentation.util.formatDistanceWithTime
import com.example.mandalunch.presentation.viewmodel.LocationSearchUiState
import com.example.mandalunch.presentation.viewmodel.RestaurantUiEvent
import com.example.mandalunch.presentation.viewmodel.RestaurantUiState
import com.example.mandalunch.presentation.viewmodel.RestaurantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantScreen(
    onBack: () -> Unit,
    onRespin: () -> Unit = {},
    viewModel: RestaurantViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val locationState by viewModel.locationSearchState.collectAsStateWithLifecycle()
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val menuName = viewModel.menuNameForDisplay

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(granted)
    }

    // 초기 진입 시 GPS 권한 요청
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is RestaurantUiEvent.ShareToKakao ->
                    KakaoShareLauncher.share(context.findActivity(), ev.message)
                is RestaurantUiEvent.ShowToast ->
                    Toast.makeText(context, ev.message, Toast.LENGTH_SHORT).show()
                RestaurantUiEvent.RequestGpsPermission ->
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                ),
                title = {
                    Column {
                        Text(
                            text = "근처 음식점",
                            color = TextPrimary,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        )
                        if (menuName.isNotBlank()) {
                            Text(
                                text = "\"$menuName\" 기준 검색",
                                color = TextDim,
                                style = TextStyle(fontSize = 12.sp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onBack)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "← 돌아가기",
                            color = TextPrimary,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(innerPadding)
        ) {
            // 위치 검색 바 + 저장된 위치 칩
            LocationSearchBar(
                locationState = locationState,
                savedLocations = savedLocations,
                onQueryChange = viewModel::onLocationQueryChange,
                onSearch = viewModel::onSearchByLocation,
                onUseCurrentLocation = viewModel::onUseCurrentLocation,
                onSaveLocation = viewModel::onSaveCurrentLocation,
                onSelectSaved = viewModel::onSelectSavedLocation,
                onDeleteSaved = viewModel::onDeleteSavedLocation
            )

            // 메인 콘텐츠
            Box(modifier = Modifier.weight(1f)) {
                when (val s = state) {
                    is RestaurantUiState.Loading -> LoadingContent()
                    is RestaurantUiState.PermissionDenied -> PermissionDeniedContent(
                        onOpenSettings = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(intent)
                        },
                        onRetry = {
                            viewModel.retry()
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                    is RestaurantUiState.Error -> ErrorContent(
                        message = s.message,
                        onRetry = {
                            viewModel.retry()
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                    is RestaurantUiState.Success -> SuccessContent(
                        restaurants = s.restaurants,
                        onRestaurantClick = { url ->
                            if (url.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        },
                        onShareClick = viewModel::onShareRestaurant,
                        onRespin = onRespin
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationSearchBar(
    locationState: LocationSearchUiState,
    savedLocations: List<SavedLocation>,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onSaveLocation: () -> Unit,
    onSelectSaved: (SavedLocation) -> Unit,
    onDeleteSaved: (Int) -> Unit
) {
    val isGpsMode = locationState.customCoords == null
    val canSave = !isGpsMode && locationState.customCoords != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // GPS 현재위치 chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isGpsMode) AccentOrange.copy(alpha = 0.15f) else Surface2Dark)
                    .border(
                        1.dp,
                        if (isGpsMode) AccentOrange.copy(alpha = 0.6f) else TextDim.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(onClick = onUseCurrentLocation)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📍", style = TextStyle(fontSize = 15.sp))
            }

            // 위치명 입력 필드
            OutlinedTextField(
                value = locationState.query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "지역 검색 (예: 강남역, 판교)",
                        color = TextDim,
                        style = TextStyle(fontSize = 13.sp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                trailingIcon = {
                    if (locationState.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 4.dp),
                            strokeWidth = 2.dp,
                            color = AccentOrange
                        )
                    } else if (locationState.query.isNotBlank()) {
                        IconButton(onClick = onSearch) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "위치 검색",
                                tint = AccentOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
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

            // 저장 버튼 (커스텀 위치 검색 성공 시에만 표시)
            if (canSave) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentOrange.copy(alpha = 0.15f))
                        .clickable(onClick = onSaveLocation)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔖", style = TextStyle(fontSize = 15.sp))
                }
            }
        }

        // 에러 메시지
        if (locationState.error != null) {
            Text(
                text = locationState.error,
                color = AccentRed,
                style = TextStyle(fontSize = 12.sp),
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }

        // 현재 검색 기준 위치
        val labelText = if (isGpsMode) "현재 위치 기준" else "\"${locationState.label}\" 기준"
        Text(
            text = labelText,
            color = if (isGpsMode) TextDim.copy(alpha = 0.6f) else AccentOrange.copy(alpha = 0.8f),
            style = TextStyle(fontSize = 11.sp),
            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
        )

        // 저장된 위치 칩 목록
        if (savedLocations.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                savedLocations.forEach { loc ->
                    SavedLocationChip(
                        label = loc.label,
                        isActive = locationState.label == loc.label && !isGpsMode,
                        onClick = { onSelectSaved(loc) },
                        onDelete = { onDeleteSaved(loc.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedLocationChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) AccentOrange.copy(alpha = 0.2f) else Surface2Dark)
            .border(
                1.dp,
                if (isActive) AccentOrange.copy(alpha = 0.7f) else TextDim.copy(alpha = 0.25f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔖",
            style = TextStyle(fontSize = 10.sp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            color = if (isActive) AccentOrange else TextPrimary,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
        )
        Spacer(Modifier.width(2.dp))
        // X 삭제 버튼
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                color = TextDim,
                style = TextStyle(fontSize = 10.sp)
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = AccentOrange, strokeWidth = 3.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "음식점을 검색하는 중...",
            color = TextDim,
            style = TextStyle(fontSize = 14.sp)
        )
    }
}

@Composable
private fun PermissionDeniedContent(onOpenSettings: () -> Unit, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📍", style = TextStyle(fontSize = 56.sp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "위치 권한이 필요해요",
            color = TextPrimary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "주변 음식점을 찾기 위해 위치 권한을 허용해주세요.\n설정에서 권한을 켠 뒤 다시 시도해주세요.",
            color = TextDim,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp)
        )
        Spacer(Modifier.height(28.dp))
        SpinButton(text = "⚙️ 권한 설정 열기", backgroundColor = AccentRed, contentColor = TextPrimary, onClick = onOpenSettings)
        Spacer(Modifier.height(12.dp))
        SpinButton(text = "🔄 다시 시도", backgroundColor = SurfaceDark, contentColor = TextPrimary, onClick = onRetry)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⚠️", style = TextStyle(fontSize = 56.sp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "문제가 발생했어요",
            color = TextPrimary,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(text = message, color = TextDim, textAlign = TextAlign.Center, style = TextStyle(fontSize = 14.sp))
        Spacer(Modifier.height(28.dp))
        SpinButton(text = "🔄 다시 시도", backgroundColor = AccentOrange, contentColor = TextPrimary, onClick = onRetry)
    }
}

@Composable
private fun SuccessContent(
    restaurants: List<Restaurant>,
    onRestaurantClick: (String) -> Unit,
    onShareClick: (Restaurant) -> Unit,
    onRespin: () -> Unit = {}
) {
    if (restaurants.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🍽️", style = TextStyle(fontSize = 56.sp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = "근처에 관련 음식점이 없어요 😕",
                color = TextDim,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 16.sp)
            )
            Spacer(Modifier.height(28.dp))
            SpinButton(
                text = "🎲 다시 뽑기",
                backgroundColor = AccentOrange,
                contentColor = TextPrimary,
                onClick = onRespin
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Text(
                text = "총 ${restaurants.size}곳 발견",
                color = TextDim,
                style = TextStyle(fontSize = 12.sp),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }

        itemsIndexed(
            items = restaurants,
            key = { _, r -> r.placeUrl.ifBlank { r.placeName + r.addressName } }
        ) { index, restaurant ->
            RestaurantCard(
                rank = index + 1,
                restaurant = restaurant,
                onClick = { onRestaurantClick(restaurant.placeUrl) },
                onShareClick = { onShareClick(restaurant) }
            )
            if (index < restaurants.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = Surface2Dark,
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
private fun RestaurantCard(
    rank: Int,
    restaurant: Restaurant,
    onClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val distColor = distanceColor(restaurant.distanceMeters)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (rank <= 3) AccentOrange.copy(alpha = 0.15f) else Surface2Dark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                color = if (rank <= 3) AccentOrange else TextDim,
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = restaurant.placeName,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            )

            val category = cleanCategory(restaurant.categoryName)
            if (category.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = category,
                    color = AccentOrange.copy(alpha = 0.8f),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
                )
            }

            Spacer(Modifier.height(6.dp))

            val address = restaurant.roadAddressName.ifBlank { restaurant.addressName }
            if (address.isNotBlank()) {
                Text(
                    text = address,
                    color = TextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontSize = 12.sp)
                )
                Spacer(Modifier.height(6.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, distColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .background(distColor.copy(alpha = 0.08f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "📍 ${formatDistanceWithTime(restaurant.distanceMeters)}",
                        color = distColor,
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                }

                if (restaurant.phone.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = restaurant.phone,
                        color = TextDim.copy(alpha = 0.7f),
                        style = TextStyle(fontSize = 11.sp)
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "카카오톡으로 공유",
                    tint = TextDim,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "›",
                color = TextDim,
                style = TextStyle(fontSize = 22.sp)
            )
        }
    }
}

private fun distanceColor(meters: Int): Color = when {
    meters <= 500  -> AccentGreen
    meters <= 1500 -> AccentOrange
    else           -> AccentBlue
}
