package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.repository.RestaurantRepository
import javax.inject.Inject

class SearchLocationByNameUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(query: String): Result<Coordinates> = runCatching {
        restaurantRepository.searchCoordinatesByName(query)
    }
}
