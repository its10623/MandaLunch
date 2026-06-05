package com.example.mandalunch.domain.model

data class Menu(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val isFavorite: Boolean = false,
    val lastRecommendedAt: Long? = null
)
