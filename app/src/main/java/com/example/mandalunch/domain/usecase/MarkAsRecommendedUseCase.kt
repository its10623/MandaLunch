package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.repository.MenuRepository
import javax.inject.Inject

class MarkAsRecommendedUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(menuId: Int, timestamp: Long) =
        repository.updateLastRecommended(menuId, timestamp)
}
