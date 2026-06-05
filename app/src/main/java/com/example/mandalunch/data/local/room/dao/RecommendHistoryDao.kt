package com.example.mandalunch.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mandalunch.data.local.room.entity.RecommendHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendHistoryDao {
    @Query("SELECT * FROM recommend_histories ORDER BY recommendedAt DESC")
    fun getAll(): Flow<List<RecommendHistoryEntity>>

    @Insert
    suspend fun insert(history: RecommendHistoryEntity)

    @Query("DELETE FROM recommend_histories")
    suspend fun deleteAll()
}
