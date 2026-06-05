package com.example.mandalunch.domain.model

data class RecommendHistory(
    val id: Int = 0,
    val menuId: Int,
    val menuName: String,
    val categoryName: String,
    val recommendedAt: Long
)
