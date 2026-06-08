package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.repository.MenuRepository
import javax.inject.Inject

/**
 * 새 메뉴를 풀(isOnBoard=false)에 추가한다.
 * 보드에 직접 추가하지 않으며, 사용자는 편집 화면에서 명시적으로 보드에 올린다.
 */
class AddMenuToPoolUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(name: String, categoryId: Int): Long {
        require(name.isNotBlank()) { "Menu name must not be blank" }
        return repository.insertMenu(
            Menu(
                id = 0,
                name = name.trim(),
                categoryId = categoryId,
                isOnBoard = false
            )
        )
    }
}
