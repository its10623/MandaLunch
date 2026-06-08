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
        // 1차: 키워드 검색 (장소명, 지하철역 등)
        val keywordDoc = apiService.searchByName(query, size = 1).documents.firstOrNull()
        if (keywordDoc != null) {
            val lat = keywordDoc.y.toDoubleOrNull()
            val lng = keywordDoc.x.toDoubleOrNull()
            if (lat != null && lng != null) return Coordinates(lat, lng)
        }

        // 2차: 주소 검색 폴백 (도로명/지번 주소 전체 입력 시)
        val addressDoc = apiService.searchAddress(query, size = 1).documents.firstOrNull()
            ?: throw NoSuchElementException("'$query' 위치를 찾을 수 없습니다")
        return Coordinates(
            latitude = addressDoc.y.toDoubleOrNull() ?: throw IllegalStateException("잘못된 위치 데이터"),
            longitude = addressDoc.x.toDoubleOrNull() ?: throw IllegalStateException("잘못된 위치 데이터")
        )
    }
}
