package com.example.mandalunch.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.model.Restaurant
import com.example.mandalunch.domain.model.ShareMessage
import com.example.mandalunch.domain.usecase.GetCurrentLocationUseCase
import com.example.mandalunch.domain.usecase.SearchLocationByNameUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RestaurantUiState {
    object Loading : RestaurantUiState()
    object PermissionDenied : RestaurantUiState()
    data class Success(
        val menuName: String,
        val restaurants: List<Restaurant>
    ) : RestaurantUiState()
    data class Error(val message: String) : RestaurantUiState()
}

sealed class RestaurantUiEvent {
    data class ShareToKakao(val message: ShareMessage) : RestaurantUiEvent()
    object RequestGpsPermission : RestaurantUiEvent()
}

data class LocationSearchUiState(
    val query: String = "",
    val label: String = "현재 위치",
    val isSearching: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val searchNearbyRestaurantsUseCase: SearchNearbyRestaurantsUseCase,
    private val searchLocationByNameUseCase: SearchLocationByNameUseCase
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

    private val _locationSearchState = MutableStateFlow(LocationSearchUiState())
    val locationSearchState: StateFlow<LocationSearchUiState> = _locationSearchState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            _uiState.value = RestaurantUiState.PermissionDenied
            return
        }
        loadByGps()
    }

    fun onLocationQueryChange(query: String) {
        _locationSearchState.update { it.copy(query = query, error = null) }
    }

    fun onSearchByLocation() {
        val query = _locationSearchState.value.query.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            _locationSearchState.update { it.copy(isSearching = true, error = null) }
            searchLocationByNameUseCase(query)
                .onSuccess { coords ->
                    _locationSearchState.update {
                        it.copy(isSearching = false, label = query, error = null)
                    }
                    loadRestaurantsWithCoords(coords)
                }
                .onFailure {
                    _locationSearchState.update {
                        it.copy(isSearching = false, error = "'$query' 위치를 찾을 수 없어요")
                    }
                }
        }
    }

    fun onUseCurrentLocation() {
        _locationSearchState.value = LocationSearchUiState()
        viewModelScope.launch {
            _events.emit(RestaurantUiEvent.RequestGpsPermission)
        }
    }

    fun onShareRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            val distance = formatDistanceWithTime(restaurant.distanceMeters)
            val address = restaurant.roadAddressName.ifBlank { restaurant.addressName }
            val encodedName = Uri.encode(restaurant.placeName)
            val kakaoMapUrl = "https://map.kakao.com/link/map/$encodedName,${restaurant.latitude},${restaurant.longitude}"
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

    fun retry() {
        _uiState.value = RestaurantUiState.Loading
    }

    private fun loadByGps() {
        viewModelScope.launch {
            _uiState.value = RestaurantUiState.Loading
            getCurrentLocationUseCase()
                .onSuccess { coords -> doSearchRestaurants(coords) }
                .onFailure { e ->
                    _uiState.value = RestaurantUiState.Error(
                        e.message ?: "현재 위치를 가져오지 못했습니다."
                    )
                }
        }
    }

    private fun loadRestaurantsWithCoords(coords: Coordinates) {
        viewModelScope.launch {
            _uiState.value = RestaurantUiState.Loading
            doSearchRestaurants(coords)
        }
    }

    private suspend fun doSearchRestaurants(coords: Coordinates) {
        val menuList = searchNearbyRestaurantsUseCase("$menuNameForDisplay 맛집", coords).getOrNull()
        if (!menuList.isNullOrEmpty()) {
            _uiState.value = RestaurantUiState.Success(menuNameForDisplay, menuList)
            return
        }
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
