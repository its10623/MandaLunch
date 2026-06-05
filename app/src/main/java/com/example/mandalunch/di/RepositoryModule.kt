package com.example.mandalunch.di

import com.example.mandalunch.data.repository.CategoryRepositoryImpl
import com.example.mandalunch.data.repository.LocationRepositoryImpl
import com.example.mandalunch.data.repository.MenuRepositoryImpl
import com.example.mandalunch.data.repository.RecommendHistoryRepositoryImpl
import com.example.mandalunch.data.repository.RestaurantRepositoryImpl
import com.example.mandalunch.domain.repository.CategoryRepository
import com.example.mandalunch.domain.repository.LocationRepository
import com.example.mandalunch.domain.repository.MenuRepository
import com.example.mandalunch.domain.repository.RecommendHistoryRepository
import com.example.mandalunch.domain.repository.RestaurantRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindMenuRepository(
        impl: MenuRepositoryImpl
    ): MenuRepository

    @Binds
    @Singleton
    abstract fun bindRecommendHistoryRepository(
        impl: RecommendHistoryRepositoryImpl
    ): RecommendHistoryRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(
        impl: RestaurantRepositoryImpl
    ): RestaurantRepository
}
