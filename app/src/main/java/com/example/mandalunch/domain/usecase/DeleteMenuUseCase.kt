package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.repository.MenuRepository
import javax.inject.Inject

/**
 * 메뉴를 영구 삭제한다.
 * 호출 측(ViewModel)에서 풀 메뉴 한정으로 제한한다.
 */
class DeleteMenuUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(menu: Menu) {
        repository.deleteMenu(menu)
    }
}
