package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.repository.MenuRepository
import javax.inject.Inject

/**
 * 카테고리의 보드 멤버십을 8개 menuId 목록으로 갱신한다(트랜잭션).
 * 정확히 8개일 때만 호출되어야 하며, 위반 시 IllegalArgumentException.
 */
class UpdateBoardMenusUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(categoryId: Int, boardIds: List<Int>) {
        require(boardIds.size == 8) { "Board must contain exactly 8 menus, got ${boardIds.size}" }
        repository.setBoardMenusForCategory(categoryId, boardIds)
    }
}
