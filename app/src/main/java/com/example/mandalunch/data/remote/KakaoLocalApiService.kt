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
        @Query("sort") sort: String = "accuracy"  // accuracy(관련도순) | distance(거리순)
    ): KakaoSearchResponseDto
}
