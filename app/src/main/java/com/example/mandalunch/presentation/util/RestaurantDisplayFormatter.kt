package com.example.mandalunch.presentation.util

import kotlin.math.roundToInt

/**
 * 음식점 표시 관련 순수 포맷팅 함수 모음.
 *
 * RestaurantScreen.kt에서 추출되었다. Compose/Android 의존성이 없어
 * JVM 단위 테스트로 검증 가능하다.
 */

/**
 * 카카오 Local API의 카테고리 문자열을 정제하여 핵심 카테고리만 추출한다.
 *
 * 예:
 *  - "음식점 > 한식" → "한식"
 *  - "음식점 > 한식 > 고기요리" → "한식" (두 번째 토큰)
 *  - "한식" → "한식"
 *  - "" → ""
 */
internal fun cleanCategory(raw: String): String {
    if (raw.isBlank()) return ""
    val parts = raw.split(" > ").map { it.trim() }
    return when {
        parts.size >= 2 -> parts[1]
        else -> parts[0]
    }
}

/**
 * 거리(미터)를 사용자 친화적인 "거리 · 이동 시간" 문자열로 포매팅한다.
 *
 * 규칙:
 *  - 1000m 미만: "{meters}m · 도보 {min}분"
 *  - 1000m 이상 2000m 미만: "{km}km · 도보 {min}분"
 *  - 2000m 이상: "{km}km · 차로 {min}분"
 *
 * 도보 분 계산: round(meters / 67) (최소 1분)
 * 차로 분 계산: round(meters / 500) (최소 1분)
 */
internal fun formatDistanceWithTime(meters: Int): String {
    val walkMinRaw = (meters / 67.0).roundToInt().coerceAtLeast(1)
    return if (meters < 2000) {
        val dist = if (meters >= 1000) "${"%.1f".format(meters / 1000.0)}km" else "${meters}m"
        "$dist · 도보 ${walkMinRaw}분"
    } else {
        val driveMin = (meters / 500.0).roundToInt().coerceAtLeast(1)
        "${"%.1f".format(meters / 1000.0)}km · 차로 ${driveMin}분"
    }
}
