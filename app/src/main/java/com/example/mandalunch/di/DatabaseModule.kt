package com.example.mandalunch.di

import android.content.Context
import com.example.mandalunch.data.local.room.MandaLunchDatabase
import com.example.mandalunch.data.local.room.dao.CategoryDao
import com.example.mandalunch.data.local.room.dao.MenuDao
import com.example.mandalunch.data.local.room.dao.RecommendHistoryDao
import com.example.mandalunch.data.local.room.dao.SavedLocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MandaLunchDatabase =
        MandaLunchDatabase.build(context)

    @Provides
    fun provideCategoryDao(db: MandaLunchDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideMenuDao(db: MandaLunchDatabase): MenuDao = db.menuDao()

    @Provides
    fun provideRecommendHistoryDao(db: MandaLunchDatabase): RecommendHistoryDao =
        db.recommendHistoryDao()

    @Provides
    fun provideSavedLocationDao(db: MandaLunchDatabase): SavedLocationDao =
        db.savedLocationDao()
}
