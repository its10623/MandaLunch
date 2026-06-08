package com.example.mandalunch.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mandalunch.data.local.room.dao.CategoryDao
import com.example.mandalunch.data.local.room.dao.MenuDao
import com.example.mandalunch.data.local.room.dao.RecommendHistoryDao
import com.example.mandalunch.data.local.room.dao.SavedLocationDao
import com.example.mandalunch.data.local.room.entity.CategoryEntity
import com.example.mandalunch.data.local.room.entity.MenuEntity
import com.example.mandalunch.data.local.room.entity.RecommendHistoryEntity
import com.example.mandalunch.data.local.room.entity.SavedLocationEntity

@Database(
    entities = [
        CategoryEntity::class,
        MenuEntity::class,
        RecommendHistoryEntity::class,
        SavedLocationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MandaLunchDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun menuDao(): MenuDao
    abstract fun recommendHistoryDao(): RecommendHistoryDao
    abstract fun savedLocationDao(): SavedLocationDao

    companion object {
        private const val DB_NAME = "mandalunch.db"

        fun build(context: Context): MandaLunchDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MandaLunchDatabase::class.java,
                DB_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedData(db)
                    }
                })
                .build()
        }

        private fun seedData(db: SupportSQLiteDatabase) {
            // categories: 8개 (position 0..7) — PRD v2 §5 이모지
            val categories = listOf(
                Triple(1, "한식" to "🍚", 0),
                Triple(2, "중식" to "🥢", 1),
                Triple(3, "일식" to "🍣", 2),
                Triple(4, "양식" to "🍝", 3),
                Triple(5, "아시안" to "🌏", 4),
                Triple(6, "분식" to "🥡", 5),
                Triple(7, "패스트푸드" to "🏪", 6),
                Triple(8, "건강식" to "🥗", 7)
            )
            categories.forEach { (id, nameEmoji, pos) ->
                val (name, emoji) = nameEmoji
                db.execSQL(
                    "INSERT INTO categories (id, name, emoji, position) VALUES (?, ?, ?, ?)",
                    arrayOf<Any>(id, name, emoji, pos)
                )
            }

            // 보드 메뉴(카테고리당 8개): isOnBoard=1
            MenuSeedData.BOARD_MENUS_BY_CATEGORY.forEach { (categoryId, names) ->
                names.forEach { name ->
                    db.execSQL(
                        "INSERT INTO menus (name, categoryId, isFavorite, lastRecommendedAt, isOnBoard) " +
                            "VALUES (?, ?, 0, NULL, 1)",
                        arrayOf<Any>(name, categoryId)
                    )
                }
            }

            // 풀 메뉴(카테고리당 22개): isOnBoard=0
            MenuSeedData.POOL_MENUS_BY_CATEGORY.forEach { (categoryId, names) ->
                names.forEach { name ->
                    db.execSQL(
                        "INSERT INTO menus (name, categoryId, isFavorite, lastRecommendedAt, isOnBoard) " +
                            "VALUES (?, ?, 0, NULL, 0)",
                        arrayOf<Any>(name, categoryId)
                    )
                }
            }
        }
    }
}
