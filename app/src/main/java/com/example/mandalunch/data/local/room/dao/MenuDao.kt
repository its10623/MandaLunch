package com.example.mandalunch.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mandalunch.data.local.room.entity.MenuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM menus WHERE categoryId = :categoryId")
    fun getByCategory(categoryId: Int): Flow<List<MenuEntity>>

    @Query("UPDATE menus SET lastRecommendedAt = :timestamp WHERE id = :menuId")
    suspend fun updateLastRecommended(menuId: Int, timestamp: Long)

    @Query("UPDATE menus SET isFavorite = :isFavorite WHERE id = :menuId")
    suspend fun updateFavorite(menuId: Int, isFavorite: Boolean)

    @Query("SELECT * FROM menus WHERE isFavorite = 1 ORDER BY categoryId ASC, name ASC")
    fun getFavorites(): Flow<List<MenuEntity>>

    @Query("UPDATE menus SET isFavorite = 0 WHERE isFavorite = 1")
    suspend fun clearAllFavorites()
}
