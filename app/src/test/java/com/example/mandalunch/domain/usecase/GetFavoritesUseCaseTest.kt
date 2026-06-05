package com.example.mandalunch.domain.usecase

import app.cash.turbine.test
import com.example.mandalunch.domain.model.Menu
import com.example.mandalunch.domain.repository.MenuRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GetFavoritesUseCaseTest {

    private val repository: MenuRepository = mockk(relaxed = true)
    private val useCase = GetFavoritesUseCase(repository)

    @Test
    fun invoke_repositoryEmitsFavorites_emitsSameList() = runTest {
        val expected = listOf(
            Menu(id = 1, name = "비빔밥", categoryId = 1, isFavorite = true),
            Menu(id = 2, name = "짜장면", categoryId = 2, isFavorite = true),
            Menu(id = 3, name = "초밥", categoryId = 3, isFavorite = true)
        )
        every { repository.getFavorites() } returns flowOf(expected)

        useCase().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun invoke_repositoryEmitsEmptyList_emitsEmptyList() = runTest {
        every { repository.getFavorites() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Menu>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun invoke_callsRepositoryGetFavoritesExactlyOnce() = runTest {
        every { repository.getFavorites() } returns flowOf(emptyList())

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        verify(exactly = 1) { repository.getFavorites() }
    }
}
