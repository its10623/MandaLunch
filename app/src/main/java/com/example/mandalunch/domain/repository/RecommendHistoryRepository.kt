package com.example.mandalunch.domain.repository

import com.example.mandalunch.domain.model.RecommendHistory
import kotlinx.coroutines.flow.Flow

interface RecommendHistoryRepository {
    fun getHistories(): Flow<List<RecommendHistory>>
    suspend fun saveHistory(history: RecommendHistory)
    suspend fun deleteAllHistories()
}
