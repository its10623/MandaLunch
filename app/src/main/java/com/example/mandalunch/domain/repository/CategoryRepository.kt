package com.example.mandalunch.domain.repository

import com.example.mandalunch.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
}
