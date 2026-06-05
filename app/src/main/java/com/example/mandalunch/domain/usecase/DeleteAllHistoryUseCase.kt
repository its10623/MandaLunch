package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.repository.RecommendHistoryRepository
import javax.inject.Inject

class DeleteAllHistoryUseCase @Inject constructor(
    private val repository: RecommendHistoryRepository
) {
    suspend operator fun invoke() = repository.deleteAllHistories()
}
