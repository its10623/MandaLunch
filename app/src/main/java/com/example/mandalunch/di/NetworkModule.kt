package com.example.mandalunch.di

import com.example.mandalunch.BuildConfig
import com.example.mandalunch.data.remote.KakaoLocalApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"
    private const val AUTH_HEADER = "Authorization"

    /**
     * Authorization 헤더에 "KakaoAK {REST_API_KEY}"를 자동 부착하는 인터셉터.
     * 이미 호출 측에서 헤더를 명시했다면 그대로 둔다.
     */
    @Provides
    @Singleton
    fun provideKakaoAuthInterceptor(): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val request = if (original.header(AUTH_HEADER) == null) {
            original.newBuilder()
                .header(AUTH_HEADER, "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(KAKAO_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideKakaoLocalApiService(retrofit: Retrofit): KakaoLocalApiService =
        retrofit.create(KakaoLocalApiService::class.java)
}
