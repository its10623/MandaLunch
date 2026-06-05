package com.example.mandalunch.domain.repository

import com.example.mandalunch.domain.model.Coordinates

/**
 * 위치 정보 조회 Repository.
 *
 * 권한이 부여된 상태에서 호출해야 한다. 권한 미부여 상태에서 호출하면
 * 구현체가 [SecurityException]을 던질 수 있다.
 */
interface LocationRepository {
    /**
     * 현재 위치를 반환한다.
     *
     * @throws SecurityException 위치 권한이 없을 때
     * @throws IllegalStateException 위치를 가져올 수 없을 때 (예: GPS 비활성화)
     */
    suspend fun getCurrentLocation(): Coordinates
}
