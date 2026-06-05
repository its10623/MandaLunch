package com.example.mandalunch.domain.repository

import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.model.Restaurant

/**
 * 음식점 검색 Repository.
 */
interface RestaurantRepository {
    /**
     * 주어진 좌표 주변에서 [query] 키워드로 음식점을 검색한다.
     *
     * @param query 검색 키워드 (예: "비빔밥 음식점")
     * @param coords 검색 중심 좌표
     * @param radiusMeters 검색 반경(미터). 기본값 1000m.
     * @param size 결과 개수. 기본값 15개.
     */
    suspend fun searchNearby(
        query: String,
        coords: Coordinates,
        radiusMeters: Int = 1000,
        size: Int = 15
    ): List<Restaurant>
}
