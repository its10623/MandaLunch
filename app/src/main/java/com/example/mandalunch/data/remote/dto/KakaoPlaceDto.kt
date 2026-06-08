package com.example.mandalunch.data.remote.dto

import com.example.mandalunch.domain.model.Restaurant
import com.google.gson.annotations.SerializedName

/**
 * Kakao Local API 키워드 검색 응답 루트 DTO.
 * https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-keyword
 */
data class KakaoSearchResponseDto(
    @SerializedName("documents") val documents: List<KakaoPlaceDto>,
    @SerializedName("meta") val meta: KakaoMetaDto
)

/**
 * Kakao Local API의 개별 음식점(장소) DTO.
 */
data class KakaoPlaceDto(
    @SerializedName("place_name") val placeName: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("address_name") val addressName: String,
    @SerializedName("road_address_name") val roadAddressName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("distance") val distance: String,
    @SerializedName("place_url") val placeUrl: String,
    @SerializedName("x") val x: String, // 경도(longitude)
    @SerializedName("y") val y: String  // 위도(latitude)
)

/**
 * Kakao Local API 응답 메타데이터.
 */
data class KakaoMetaDto(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("is_end") val isEnd: Boolean
)

// 주소 검색 API 응답 DTO (v2/local/search/address.json)
data class KakaoAddressResponseDto(
    @SerializedName("documents") val documents: List<KakaoAddressDocumentDto>
)

data class KakaoAddressDocumentDto(
    @SerializedName("x") val x: String,  // 경도(longitude)
    @SerializedName("y") val y: String   // 위도(latitude)
)

/**
 * Kakao Place DTO → Domain Restaurant 매핑.
 */
fun KakaoPlaceDto.toDomain(): Restaurant = Restaurant(
    placeName = placeName,
    categoryName = categoryName,
    addressName = addressName,
    roadAddressName = roadAddressName,
    phone = phone,
    distanceMeters = distance.toIntOrNull() ?: 0,
    placeUrl = placeUrl,
    latitude = y.toDoubleOrNull() ?: 0.0,
    longitude = x.toDoubleOrNull() ?: 0.0
)
