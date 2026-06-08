package com.example.mandalunch.presentation.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.model.RecommendHistory
import com.example.mandalunch.domain.usecase.DeleteAllHistoryUseCase
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class DateGroup(val label: String) {
    TODAY("오늘"),
    YESTERDAY("어제"),
    THIS_WEEK("이번 주"),
    EARLIER("이전")
}

data class HistoryItemUi(
    val id: Int,
    val menuName: String,
    val categoryName: String,
    val categoryEmoji: String,
    val recommendedAt: Long,
    val timeLabel: String
)

data class HistorySection(
    val group: DateGroup,
    val items: List<HistoryItemUi>
)

data class HistoryUiState(
    val sections: List<HistorySection> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val showDeleteConfirm: Boolean = false
)

sealed class HistoryUiEvent {
    object NavigateBack : HistoryUiEvent()
    object DeleteAllCompleted : HistoryUiEvent()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val deleteAllHistoryUseCase: DeleteAllHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HistoryUiEvent>()
    val events: SharedFlow<HistoryUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(getHistoryUseCase(), getCategoriesUseCase()) { histories, categories ->
                histories to categories
            }.collect { (histories, categories) ->
                _uiState.update {
                    it.copy(
                        sections = groupByDate(histories, categories = categories),
                        isLoading = false,
                        isEmpty = histories.isEmpty()
                    )
                }
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch { _events.emit(HistoryUiEvent.NavigateBack) }
    }

    fun onDeleteAllClick() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun onDismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun onConfirmDeleteAll() {
        viewModelScope.launch {
            deleteAllHistoryUseCase()
            _uiState.update { it.copy(showDeleteConfirm = false) }
            _events.emit(HistoryUiEvent.DeleteAllCompleted)
        }
    }

    @VisibleForTesting
    internal fun groupByDate(
        histories: List<RecommendHistory>,
        categories: List<Category> = emptyList(),
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): List<HistorySection> {
        if (histories.isEmpty()) return emptyList()

        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val yesterday = today.minusDays(1)
        val mondayThisWeek = today.with(DayOfWeek.MONDAY)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val emojiByName = categories.associate { it.name to it.emoji }

        val grouped = histories.groupBy { h ->
            val date = Instant.ofEpochMilli(h.recommendedAt).atZone(zone).toLocalDate()
            when {
                date == today -> DateGroup.TODAY
                date == yesterday -> DateGroup.YESTERDAY
                !date.isBefore(mondayThisWeek) -> DateGroup.THIS_WEEK
                else -> DateGroup.EARLIER
            }
        }

        return DateGroup.entries
            .mapNotNull { group ->
                grouped[group]?.let { items ->
                    HistorySection(
                        group = group,
                        items = items
                            .sortedByDescending { it.recommendedAt }
                            .map { h ->
                                HistoryItemUi(
                                    id = h.id,
                                    menuName = h.menuName,
                                    categoryName = h.categoryName,
                                    categoryEmoji = emojiByName[h.categoryName] ?: "🍽️",
                                    recommendedAt = h.recommendedAt,
                                    timeLabel = Instant.ofEpochMilli(h.recommendedAt)
                                        .atZone(zone)
                                        .format(timeFormatter)
                                )
                            }
                    )
                }
            }
    }
}
