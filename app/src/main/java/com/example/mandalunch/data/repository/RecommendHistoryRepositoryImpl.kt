package com.example.mandalunch.data.repository

import com.example.mandalunch.data.local.room.dao.RecommendHistoryDao
import com.example.mandalunch.data.local.room.entity.RecommendHistoryEntity
import com.example.mandalunch.domain.model.RecommendHistory
import com.example.mandalunch.domain.repository.RecommendHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecommendHistoryRepositoryImpl @Inject constructor(
    private val dao: RecommendHistoryDao
) : RecommendHistoryRepository {
    override fun getHistories(): Flow<List<RecommendHistory>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveHistory(history: RecommendHistory) {
        dao.insert(history.toEntity())
    }

    override suspend fun deleteAllHistories() {
        dao.deleteAll()
    }

    private fun RecommendHistoryEntity.toDomain(): RecommendHistory =
        RecommendHistory(
            id = id,
            menuId = menuId,
            menuName = menuName,
            categoryName = categoryName,
            recommendedAt = recommendedAt
        )

    private fun RecommendHistory.toEntity(): RecommendHistoryEntity =
        RecommendHistoryEntity(
            id = id,
            menuId = menuId,
            menuName = menuName,
            categoryName = categoryName,
            recommendedAt = recommendedAt
        )
}
