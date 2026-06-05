package com.example.mandalunch.data.repository

import com.example.mandalunch.data.remote.FusedLocationDataSource
import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.repository.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val dataSource: FusedLocationDataSource
) : LocationRepository {

    override suspend fun getCurrentLocation(): Coordinates =
        dataSource.fetchCurrent()
}
