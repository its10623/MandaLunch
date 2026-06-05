package com.example.mandalunch.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mandalunch.data.local.room.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY position")
    fun getAll(): Flow<List<CategoryEntity>>
}
