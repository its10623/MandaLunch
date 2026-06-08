package com.example.mandalunch.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mandalunch.data.local.room.entity.MenuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    // 기존 (보존): 모든 메뉴 조회 (보드/풀 무관)
    @Query("SELECT * FROM menus WHERE categoryId = :categoryId")
    fun getByCategory(categoryId: Int): Flow<List<MenuEntity>>

    // 신규: 보드 메뉴(isOnBoard=1)만 조회
    @Query("SELECT * FROM menus WHERE categoryId = :categoryId AND isOnBoard = 1 ORDER BY id ASC")
    fun getBoardMenusByCategory(categoryId: Int): Flow<List<MenuEntity>>

    // 신규: 전체 메뉴(보드 + 풀) 조회 — 보드 우선 정렬
    @Query("SELECT * FROM menus WHERE categoryId = :categoryId ORDER BY isOnBoard DESC, id ASC")
    fun getAllMenusByCategory(categoryId: Int): Flow<List<MenuEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenu(menu: MenuEntity): Long

    @Delete
    suspend fun deleteMenu(menu: MenuEntity)

    @Query("UPDATE menus SET isOnBoard = :isOnBoard WHERE id = :menuId")
    suspend fun updateOnBoard(menuId: Int, isOnBoard: Boolean)

    /**
     * 카테고리 내 모든 메뉴의 isOnBoard를 boardIds 기준으로 일괄 갱신한다.
     * 단일 UPDATE 문이므로 SQLite 내부에서 원자적으로 처리되며, @Transaction으로 외부 일관성 보강.
     */
    @Transaction
    @Query("UPDATE menus SET isOnBoard = CASE WHEN id IN (:boardIds) THEN 1 ELSE 0 END WHERE categoryId = :categoryId")
    suspend fun setBoardMenusForCategory(categoryId: Int, boardIds: List<Int>)

    @Query("UPDATE menus SET lastRecommendedAt = :timestamp WHERE id = :menuId")
    suspend fun updateLastRecommended(menuId: Int, timestamp: Long)

    @Query("UPDATE menus SET isFavorite = :isFavorite WHERE id = :menuId")
    suspend fun updateFavorite(menuId: Int, isFavorite: Boolean)

    @Query("SELECT * FROM menus WHERE isFavorite = 1 ORDER BY categoryId ASC, name ASC")
    fun getFavorites(): Flow<List<MenuEntity>>

    @Query("UPDATE menus SET isFavorite = 0 WHERE isFavorite = 1")
    suspend fun clearAllFavorites()
}
