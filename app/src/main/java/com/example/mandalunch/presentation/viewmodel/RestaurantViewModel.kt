package com.example.mandalunch.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Restaurant
import com.example.mandalunch.domain.model.ShareMessage
import com.example.mandalunch.domain.usecase.GetCurrentLocationUseCase
import com.example.mandalunch.domain.usecase.SearchNearbyRestaurantsUseCase
import com.example.mandalunch.presentation.navigation.Routes
import com.example.mandalunch.presentation.util.formatDistanceWithTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * RestaurantScreen UiState.
 *
 *  - [Loading]          : 권한 응답 대기 또는 위치/검색 진행
 *  - [PermissionDenied] : 위치 권한 거부
 *  - [Success]          : 검색 성공(빈 리스트는 화면 측에서 별도 처리)
 *  - [Error]            : 위치 획득 실패 또는 네트워크 오류
 */
sealed class RestaurantUiState {
    object Loading : RestaurantUiState()
    object PermissionDenied : RestaurantUiState()
    data class Success(
        val menuName: String,
        val restaurants: List<Restaurant>
    ) : RestaurantUiState()
    data class Error(val message: String) : RestaurantUiState()
}

/**
 * RestaurantScreen 일회성 이벤트.
 *
 *  - [ShareToKakao] : 카카오톡 공유 요청 (Screen → KakaoShareLauncher 호출)
 */
sealed class RestaurantUiEvent {
    data class ShareToKakao(val message: ShareMessage) : RestaurantUiEvent()
}

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val searchNearbyRestaurantsUseCase: SearchNearbyRestaurantsUseCase
) : ViewModel() {

    private val rawMenuName: String =
        savedStateHandle.get<String>(Routes.ARG_RESTAURANT_MENU) ?: ""
    val menuNameForDisplay: String = Uri.decode(rawMenuName)

    private val rawCategoryName: String =
        savedStateHandle.get<String>(Routes.ARG_RESTAURANT_CATEGORY) ?: ""
    private val categoryName: String = Uri.decode(rawCategoryName)

    private val _uiState = MutableStateFlow<RestaurantUiState>(RestaurantUiState.Loading)
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RestaurantUiEvent>()
    val events: SharedFlow<RestaurantUiEvent> = _events.asSharedFlow()

    /**
     * Screen에서 권한 요청 결과를 전달한다.
     * granted = true 이면 위치 → 음식점 검색 흐름을 시작한다.
     */
    fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            _uiState.value = RestaurantUiState.PermissionDenied
            return
        }
        loadRestaurants()
    }

    fun onShareRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            val distance = formatDistanceWithTime(restaurant.distanceMeters)
            val address = restaurant.roadAddressName.ifBlank { restaurant.addressName }
            val kakaoMapUrl = restaurant.placeUrl
                .replace("http://", "https://")
                .ifBlank {
                    val encodedName = Uri.encode(restaurant.placeName)
                    "https://map.kakao.com/link/map/$encodedName,${restaurant.latitude},${restaurant.longitude}"
                }
            val message = ShareMessage(
                title = restaurant.placeName,
                description = "$address · $distance",
                linkWebUrl = kakaoMapUrl,
                linkMobileWebUrl = kakaoMapUrl,
                buttonTitle = "카카오맵에서 보기"
            )
            _events.emit(RestaurantUiEvent.ShareToKakao(message))
        }
    }

    /**
     * Error 상태 또는 PermissionDenied 상태에서 재시도.
     * Screen은 이 호출 후 권한 요청을 다시 launch 한다.
     */
    fun retry() {
        _uiState.value = RestaurantUiState.Loading
    }

    private fun loadRestaurants() {
        viewModelScope.launch {
            _uiState.value = RestaurantUiState.Loading
            getCurrentLocationUseCase()
                .onSuccess { coords ->
                    // 1차: 메뉴명으로 검색 ("버거 맛집", "훠궈 맛집" 등)
                    val menuQueryResult = searchNearbyRestaurantsUseCase("$menuNameForDisplay 맛집", coords)
                    val menuList = menuQueryResult.getOrNull()

                    if (!menuList.isNullOrEmpty()) {
                        _uiState.value = RestaurantUiState.Success(menuNameForDisplay, menuList)
                    } else {
                        // 2차 폴백: 카테고리명으로 검색 ("한식 맛집", "중식 맛집" 등)
                        searchNearbyRestaurantsUseCase("$categoryName 맛집", coords)
                            .onSuccess { list ->
                                _uiState.value = RestaurantUiState.Success(menuNameForDisplay, list)
                            }
                            .onFailure { e ->
                                _uiState.value = RestaurantUiState.Error(
                                    e.message ?: "음식점 정보를 불러오지 못했습니다."
                                )
                            }
                    }
                }
                .onFailure { e ->
                    _uiState.value = RestaurantUiState.Error(
                        e.message ?: "현재 위치를 가져오지 못했습니다."
                    )
                }
        }
    }
}
