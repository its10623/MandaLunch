package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.model.Restaurant
import com.example.mandalunch.domain.repository.RestaurantRepository
import javax.inject.Inject

/**
 * 메뉴 이름과 좌표를 받아 주변 음식점을 검색한다.
 *
 * 비즈니스 규칙:
 *  - 검색 키워드 = "$menuName 음식점"
 *  - 반경 3000m, 결과 15개
 *
 * 네트워크 오류 등 실패는 [Result.failure]로 감싸 반환한다.
 */
class SearchNearbyRestaurantsUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(
        query: String,
        coords: Coordinates
    ): Result<List<Restaurant>> = runCatching {
        restaurantRepository.searchNearby(
            query = query,
            coords = coords,
            radiusMeters = 3000,
            size = 15
        )
    }
}
