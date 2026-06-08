package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 카테고리의 전체 메뉴(보드 + 풀)를 Flow로 조회한다.
 * 메뉴 편집 화면용.
 */
class GetAllMenusByCategoryUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    operator fun invoke(categoryId: Int): Flow<List<Menu>> =
        repository.getAllMenusByCategory(categoryId)
}
