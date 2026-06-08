package com.example.mandalunch.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menus")
data class MenuEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val categoryId: Int,
    val isFavorite: Boolean = false,
    val lastRecommendedAt: Long? = null,
    val isOnBoard: Boolean = true   // true=보드(만다라트 9x9), false=풀(저장만)
)
