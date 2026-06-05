package com.example.mandalunch.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val category: Category? = null,
    val menuName: String = ""
)

sealed class ResultUiEvent {
    object NavigateBackToMandalart : ResultUiEvent()
    object NavigateBackToMenuSelect : ResultUiEvent()
    data class NavigateToRestaurant(val menuName: String, val categoryName: String) : ResultUiEvent()
}

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val categoryId: Int = savedStateHandle.get<Int>(Routes.ARG_CATEGORY_ID) ?: -1
    private val rawMenuName: String = savedStateHandle.get<String>(Routes.ARG_MENU_NAME) ?: ""
    private val menuName: String = Uri.decode(rawMenuName)

    private val _uiState = MutableStateFlow(ResultUiState(menuName = menuName))
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResultUiEvent>()
    val events: SharedFlow<ResultUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val cat = getCategoriesUseCase().first().firstOrNull { it.id == categoryId }
            _uiState.update { it.copy(category = cat) }
        }
    }

    fun onRestartClick() {
        viewModelScope.launch {
            _events.emit(ResultUiEvent.NavigateBackToMandalart)
        }
    }

    fun onSameCategoryAgainClick() {
        viewModelScope.launch {
            _events.emit(ResultUiEvent.NavigateBackToMenuSelect)
        }
    }

    fun onFindRestaurantsClick() {
        viewModelScope.launch {
            val categoryName = _uiState.value.category?.name ?: menuName
            _events.emit(ResultUiEvent.NavigateToRestaurant(menuName, categoryName))
        }
    }
}
