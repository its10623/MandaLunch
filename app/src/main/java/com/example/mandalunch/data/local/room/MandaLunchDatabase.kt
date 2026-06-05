package com.example.mandalunch.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mandalunch.data.local.room.dao.CategoryDao
import com.example.mandalunch.data.local.room.dao.MenuDao
import com.example.mandalunch.data.local.room.dao.RecommendHistoryDao
import com.example.mandalunch.data.local.room.entity.CategoryEntity
import com.example.mandalunch.data.local.room.entity.MenuEntity
import com.example.mandalunch.data.local.room.entity.RecommendHistoryEntity

@Database(
    entities = [
        CategoryEntity::class,
        MenuEntity::class,
        RecommendHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MandaLunchDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun menuDao(): MenuDao
    abstract fun recommendHistoryDao(): RecommendHistoryDao

    companion object {
        private const val DB_NAME = "mandalunch.db"

        fun build(context: Context): MandaLunchDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MandaLunchDatabase::class.java,
                DB_NAME
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedData(db)
                    }
                })
                .fallbackToDestructiveMigration(dropAllTables = true)
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

            // menus: 카테고리당 8개 = 총 64개 — PRD v2 §5
            val menusByCategory = mapOf(
                1 to listOf("된장찌개", "김치찌개", "비빔밥", "삼겹살", "순두부찌개", "불고기", "갈비탕", "냉면"),
                2 to listOf("짜장면", "짬뽕", "탕수육", "마파두부", "볶음밥", "깐풍기", "유산슬", "딤섬"),
                3 to listOf("초밥", "라멘", "돈카츠", "우동", "텐동", "오야코동", "야키토리", "타코야키"),
                4 to listOf("파스타", "스테이크", "리조또", "피자", "샌드위치", "크림수프", "뇨키", "샐러드"),
                5 to listOf("쌀국수", "팟타이", "나시고렝", "분짜", "커리", "반미", "똠얌꿍", "마라탕"),
                6 to listOf("떡볶이", "순대", "김밥", "튀김", "어묵", "라볶이", "치즈볼", "핫도그"),
                7 to listOf("햄버거", "치킨", "피자", "샌드위치", "버거킹", "맥도날드", "롯데리아", "KFC"),
                8 to listOf("닭가슴살", "그린샐러드", "아보카도볼", "두부샐러드", "현미밥", "채소비빔밥", "연어포케", "오트밀")
            )
            menusByCategory.forEach { (categoryId, names) ->
                names.forEach { name ->
                    db.execSQL(
                        "INSERT INTO menus (name, categoryId, isFavorite, lastRecommendedAt) VALUES (?, ?, ?, NULL)",
                        arrayOf<Any>(name, categoryId, 0)
                    )
                }
            }
        }
    }
}
