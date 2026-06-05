package com.example.mandalunch.domain.repository

import com.example.mandalunch.domain.model.Menu
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun getMenusByCategory(categoryId: Int): Flow<List<Menu>>
    suspend fun updateLastRecommended(menuId: Int, timestamp: Long)

    fun getFavorites(): Flow<List<Menu>>
    suspend fun setFavorite(menuId: Int, isFavorite: Boolean)
    suspend fun clearAllFavorites()
}
