package com.example.mandalunch.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommend_histories")
data class RecommendHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val menuId: Int,
    val menuName: String,
    val categoryName: String,
    val recommendedAt: Long
)
