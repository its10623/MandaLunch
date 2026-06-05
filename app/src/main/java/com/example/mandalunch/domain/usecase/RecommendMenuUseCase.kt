package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Menu
import javax.inject.Inject

class RecommendMenuUseCase @Inject constructor() {
    operator fun invoke(menus: List<Menu>): Menu? {
        if (menus.isEmpty()) return null
        // lastRecommendedAt이 null인 메뉴 우선 추천
        val unseen = menus.filter { it.lastRecommendedAt == null }
        return if (unseen.isNotEmpty()) unseen.random() else menus.random()
    }
}
