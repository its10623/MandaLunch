package com.example.mandalunch.domain.model

/**
 * 음식점 도메인 모델. Kakao Local API의 응답 DTO에서 매핑된 순수 Kotlin 모델.
 *
 * @property placeName 음식점 상호명
 * @property categoryName 카테고리 (예: "음식점 > 한식")
 * @property addressName 지번 주소
 * @property roadAddressName 도로명 주소
 * @property phone 전화번호 (없을 경우 빈 문자열)
 * @property distanceMeters 사용자 위치로부터의 거리(미터)
 * @property placeUrl 카카오맵 상세 페이지 URL
 * @property latitude 위도
 * @property longitude 경도
 */
data class Restaurant(
    val placeName: String,
    val categoryName: String,
    val addressName: String,
    val roadAddressName: String,
    val phone: String,
    val distanceMeters: Int,
    val placeUrl: String,
    val latitude: Double,
    val longitude: Double
)
