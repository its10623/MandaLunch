package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.RecommendHistory
import com.example.mandalunch.domain.repository.RecommendHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: RecommendHistoryRepository
) {
    operator fun invoke(): Flow<List<RecommendHistory>> = repository.getHistories()
}
