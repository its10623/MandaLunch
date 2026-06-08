package com.example.mandalunch.domain.repository

import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.model.Restaurant

/**
 * 음식점 검색 Repository.
 */
interface RestaurantRepository {
    suspend fun searchNearby(
        query: String,
        coords: Coordinates,
        radiusMeters: Int = 1000,
        size: Int = 15
    ): List<Restaurant>

    // 위치명 텍스트 → 좌표 변환 (카카오 키워드 검색 첫 번째 결과 활용)
    suspend fun searchCoordinatesByName(query: String): Coordinates
}
