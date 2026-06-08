package com.example.mandalunch.data.repository

import com.example.mandalunch.data.local.room.dao.MenuDao
import com.example.mandalunch.data.local.room.entity.MenuEntity
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val dao: MenuDao
) : MenuRepository {

    override fun getMenusByCategory(categoryId: Int): Flow<List<Menu>> =
        dao.getByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun getBoardMenusByCategory(categoryId: Int): Flow<List<Menu>> =
        dao.getBoardMenusByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override fun getAllMenusByCategory(categoryId: Int): Flow<List<Menu>> =
        dao.getAllMenusByCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertMenu(menu: Menu): Long =
        dao.insertMenu(menu.toEntity())

    override suspend fun deleteMenu(menu: Menu) {
        dao.deleteMenu(menu.toEntity())
    }

    override suspend fun setBoardMenusForCategory(categoryId: Int, boardIds: List<Int>) {
        dao.setBoardMenusForCategory(categoryId, boardIds)
    }

    override suspend fun updateLastRecommended(menuId: Int, timestamp: Long) {
        dao.updateLastRecommended(menuId, timestamp)
    }

    override fun getFavorites(): Flow<List<Menu>> =
        dao.getFavorites().map { list -> list.map { it.toDomain() } }

    override suspend fun setFavorite(menuId: Int, isFavorite: Boolean) {
        dao.updateFavorite(menuId, isFavorite)
    }

    override suspend fun clearAllFavorites() {
        dao.clearAllFavorites()
    }

    private fun MenuEntity.toDomain(): Menu =
        Menu(
            id = id,
            name = name,
            categoryId = categoryId,
            isFavorite = isFavorite,
            lastRecommendedAt = lastRecommendedAt,
            isOnBoard = isOnBoard
        )

    private fun Menu.toEntity(): MenuEntity =
        MenuEntity(
            id = id,
            name = name,
            categoryId = categoryId,
            isFavorite = isFavorite,
            lastRecommendedAt = lastRecommendedAt,
            isOnBoard = isOnBoard
        )
}
