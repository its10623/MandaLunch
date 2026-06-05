package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * 현재 위치(좌표)를 조회한다.
 *
 * 위치 권한이 부여된 상태에서 호출해야 한다. 권한 미부여나 GPS 비활성화 등의
 * 실패는 [Result.failure]로 감싸 반환한다.
 */
class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<Coordinates> = runCatching {
        locationRepository.getCurrentLocation()
    }
}
