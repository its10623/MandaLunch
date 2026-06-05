package com.example.mandalunch.data.repository

import com.example.mandalunch.data.remote.KakaoLocalApiService
import com.example.mandalunch.data.remote.dto.toDomain
import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.model.Restaurant
import com.example.mandalunch.domain.repository.RestaurantRepository
import javax.inject.Inject

class RestaurantRepositoryImpl @Inject constructor(
    private val apiService: KakaoLocalApiService
) : RestaurantRepository {

    override suspend fun searchNearby(
        query: String,
        coords: Coordinates,
        radiusMeters: Int,
        size: Int
    ): List<Restaurant> {
        val response = apiService.searchKeyword(
            query = query,
            longitude = coords.longitude.toString(),
            latitude = coords.latitude.toString(),
            radius = radiusMeters,
            size = size
        )
        return response.documents.map { it.toDomain() }
    }
}
