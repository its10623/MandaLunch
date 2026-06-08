package com.example.mandalunch.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2 마이그레이션.
 *
 * 1) `menus` 테이블에 `isOnBoard INTEGER NOT NULL DEFAULT 1` 컬럼 추가.
 *    → 기존 64행(카테고리당 8개)이 모두 보드(1)로 보존된다.
 * 2) 카테고리당 풀 메뉴 22개씩 총 176행을 isOnBoard=0으로 삽입.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) 컬럼 추가 (기존 행 → isOnBoard=1)
        db.execSQL("ALTER TABLE menus ADD COLUMN isOnBoard INTEGER NOT NULL DEFAULT 1")

        // 2) 풀 메뉴 INSERT (Migration.migrate는 자동 트랜잭션 안에서 실행됨)
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
