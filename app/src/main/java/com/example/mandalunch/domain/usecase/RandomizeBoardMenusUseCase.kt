package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.repository.MenuRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 카테고리 전체 메뉴 중 8개를 랜덤 선택하여 보드를 갱신한다(트랜잭션).
 * 전체 메뉴가 8개 미만이면 IllegalStateException — ViewModel에서 try-catch 후 토스트.
 */
class RandomizeBoardMenusUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(categoryId: Int) {
        val all = repository.getAllMenusByCategory(categoryId).first()
        check(all.size >= 8) { "Need at least 8 menus to randomize, got ${all.size}" }
        val picked = all.shuffled().take(8).map { it.id }
        repository.setBoardMenusForCategory(categoryId, picked)
    }
}
