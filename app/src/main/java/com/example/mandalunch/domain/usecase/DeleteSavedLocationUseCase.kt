package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.repository.SavedLocationRepository
import javax.inject.Inject

class DeleteSavedLocationUseCase @Inject constructor(
    private val repository: SavedLocationRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteLocation(id)
}
