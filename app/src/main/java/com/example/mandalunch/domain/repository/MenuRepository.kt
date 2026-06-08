package com.example.mandalunch.domain.repository

import com.example.mandalunch.domain.model.Menu
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    /** 기존: 카테고리의 전체 메뉴 (deprecated 대상이지만 보존) */
    fun getMenusByCategory(categoryId: Int): Flow<List<Menu>>

    /** 신규: 카테고리의 보드 메뉴(isOnBoard=true) Flow 조회 */
    fun getBoardMenusByCategory(categoryId: Int): Flow<List<Menu>>

    /** 신규: 카테고리의 전체 메뉴(보드+풀) Flow 조회 */
    fun getAllMenusByCategory(categoryId: Int): Flow<List<Menu>>

    /** 신규: 메뉴 insert. Menu.isOnBoard 값을 그대로 사용. autoGenerate된 row id를 반환. */
    suspend fun insertMenu(menu: Menu): Long

    /** 신규: 메뉴 영구 삭제 */
    suspend fun deleteMenu(menu: Menu)

    /** 신규: 카테고리 보드 멤버십 일괄 갱신(트랜잭션). boardIds에 포함된 메뉴만 isOnBoard=1. */
    suspend fun setBoardMenusForCategory(categoryId: Int, boardIds: List<Int>)

    suspend fun updateLastRecommended(menuId: Int, timestamp: Long)

    fun getFavorites(): Flow<List<Menu>>
    suspend fun setFavorite(menuId: Int, isFavorite: Boolean)
    suspend fun clearAllFavorites()
}
