package com.example.mandalunch.data.repository

import com.example.mandalunch.data.local.room.dao.CategoryDao
import com.example.mandalunch.data.local.room.entity.CategoryEntity
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {
    override fun getCategories(): Flow<List<Category>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    private fun CategoryEntity.toDomain(): Category =
        Category(id = id, name = name, emoji = emoji, position = position)
}
