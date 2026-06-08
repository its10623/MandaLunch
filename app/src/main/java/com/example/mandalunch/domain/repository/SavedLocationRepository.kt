package com.example.mandalunch.domain.repository

import com.example.mandalunch.domain.model.SavedLocation
import kotlinx.coroutines.flow.Flow

interface SavedLocationRepository {
    fun getSavedLocations(): Flow<List<SavedLocation>>
    suspend fun saveLocation(label: String, latitude: Double, longitude: Double)
    suspend fun deleteLocation(id: Int)
    suspend fun isAlreadySaved(label: String): Boolean
}
