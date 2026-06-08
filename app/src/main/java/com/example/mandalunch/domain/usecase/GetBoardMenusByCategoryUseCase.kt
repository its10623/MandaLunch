package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 카테고리의 보드 메뉴(isOnBoard=true)만 Flow로 조회한다.
 * 9x9 만다라트 표시 및 메뉴 스핀용.
 */
class GetBoardMenusByCategoryUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    operator fun invoke(categoryId: Int): Flow<List<Menu>> =
        repository.getBoardMenusByCategory(categoryId)
}
