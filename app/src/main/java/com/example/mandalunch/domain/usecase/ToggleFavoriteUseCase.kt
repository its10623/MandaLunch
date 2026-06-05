package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.repository.MenuRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(menuId: Int, isFavorite: Boolean) {
        repository.setFavorite(menuId, isFavorite)
    }
}
