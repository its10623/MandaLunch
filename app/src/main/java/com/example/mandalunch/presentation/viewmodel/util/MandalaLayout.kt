package com.example.mandalunch.presentation.viewmodel.util

import com.example.mandalunch.domain.model.Category

object MandalaLayout {

    /** CW index(0..7, 시계방향) → DB position 변환 */
    val CW_TO_POSITION = intArrayOf(0, 1, 2, 4, 7, 6, 5, 3)

    /** gridPos(row-major 0..8) → CW index. gridPos 4는 중심(-1). */
    val GRID_TO_CW = intArrayOf(0, 1, 2, 7, -1, 3, 6, 5, 4)

    fun cwIndexToCategory(cwIndex: Int, categories: List<Category>): Category? {
        if (cwIndex !in 0..7) return null
        val targetPosition = CW_TO_POSITION[cwIndex]
        return categories.firstOrNull { it.position == targetPosition }
    }
}
