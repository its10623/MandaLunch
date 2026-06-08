package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.repository.SavedLocationRepository
import javax.inject.Inject

class SaveLocationUseCase @Inject constructor(
    private val repository: SavedLocationRepository
) {
    suspend operator fun invoke(label: String, coords: Coordinates): Result<Unit> = runCatching {
        if (repository.isAlreadySaved(label)) {
            throw IllegalStateException("\"$label\"은 이미 저장된 위치입니다")
        }
        repository.saveLocation(label, coords.latitude, coords.longitude)
    }
}
