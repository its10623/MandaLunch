package com.example.mandalunch.data.remote

import com.example.mandalunch.data.remote.dto.KakaoSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoLocalApiService {

    @GET("v2/local/search/keyword.json")
    suspend fun searchKeyword(
        @Query("query") query: String,
        @Query("x") longitude: String,
        @Query("y") latitude: String,
        @Query("radius") radius: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String = "accuracy"
    ): KakaoSearchResponseDto

    // 좌표 없이 위치명으로만 검색 — 결과 첫 번째 document의 x/y를 좌표로 활용
    @GET("v2/local/search/keyword.json")
    suspend fun searchByName(
        @Query("query") query: String,
        @Query("size") size: Int = 1
    ): KakaoSearchResponseDto
}
