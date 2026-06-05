package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    operator fun invoke(): Flow<List<Menu>> = repository.getFavorites()
}
