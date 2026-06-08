package com.example.mandalunch.data.repository

import com.example.mandalunch.data.local.room.dao.SavedLocationDao
import com.example.mandalunch.data.local.room.entity.SavedLocationEntity
import com.example.mandalunch.domain.model.SavedLocation
import com.example.mandalunch.domain.repository.SavedLocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SavedLocationRepositoryImpl @Inject constructor(
    private val dao: SavedLocationDao
) : SavedLocationRepository {

    override fun getSavedLocations(): Flow<List<SavedLocation>> =
        dao.getAll().map { list ->
            list.map { SavedLocation(it.id, it.label, it.latitude, it.longitude) }
        }

    override suspend fun saveLocation(label: String, latitude: Double, longitude: Double) {
        dao.insert(SavedLocationEntity(label = label, latitude = latitude, longitude = longitude))
    }

    override suspend fun deleteLocation(id: Int) {
        dao.deleteById(id)
    }

    override suspend fun isAlreadySaved(label: String): Boolean =
        dao.countByLabel(label) > 0
}
