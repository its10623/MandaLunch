package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.RecommendHistory
import com.example.mandalunch.domain.repository.RecommendHistoryRepository
import javax.inject.Inject

class SaveHistoryUseCase @Inject constructor(
    private val repository: RecommendHistoryRepository
) {
    suspend operator fun invoke(history: RecommendHistory) {
        repository.saveHistory(history)
    }
}
