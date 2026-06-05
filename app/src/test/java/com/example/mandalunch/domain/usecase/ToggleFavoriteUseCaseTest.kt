package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.repository.MenuRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ToggleFavoriteUseCaseTest {

    private val repository: MenuRepository = mockk(relaxed = true)
    private val useCase = ToggleFavoriteUseCase(repository)

    @Test
    fun invoke_setFavoriteTrue_callsRepositorySetFavoriteWithTrue() = runTest {
        useCase(menuId = 5, isFavorite = true)

        coVerify(exactly = 1) { repository.setFavorite(5, true) }
    }

    @Test
    fun invoke_setFavoriteFalse_callsRepositorySetFavoriteWithFalse() = runTest {
        useCase(menuId = 5, isFavorite = false)

        coVerify(exactly = 1) { repository.setFavorite(5, false) }
    }
}
