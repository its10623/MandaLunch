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

    override suspend fun searchCoordinatesByName(query: String): Coordinates {
        val response = apiService.searchByName(query, size = 1)
        val doc = response.documents.firstOrNull()
            ?: throw NoSuchElementException("'$query' 위치를 찾을 수 없습니다")
        return Coordinates(
            latitude = doc.y.toDoubleOrNull() ?: throw IllegalStateException("잘못된 위치 데이터"),
            longitude = doc.x.toDoubleOrNull() ?: throw IllegalStateException("잘못된 위치 데이터")
        )
    }
}
