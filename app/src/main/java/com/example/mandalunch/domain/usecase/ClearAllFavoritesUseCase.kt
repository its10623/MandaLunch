package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.repository.MenuRepository
import javax.inject.Inject

class ClearAllFavoritesUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke() {
        repository.clearAllFavorites()
    }
}
