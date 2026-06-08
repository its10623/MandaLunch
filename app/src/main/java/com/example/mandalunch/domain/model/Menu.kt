package com.example.mandalunch.domain.model

data class Menu(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val isFavorite: Boolean = false,
    val lastRecommendedAt: Long? = null,
    val isOnBoard: Boolean = true   // true=만다라트 9x9에 노출, false=풀(저장만)
)
