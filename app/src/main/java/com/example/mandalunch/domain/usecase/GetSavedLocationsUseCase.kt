package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.SavedLocation
import com.example.mandalunch.domain.repository.SavedLocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedLocationsUseCase @Inject constructor(
    private val repository: SavedLocationRepository
) {
    operator fun invoke(): Flow<List<SavedLocation>> = repository.getSavedLocations()
}
