package com.example.mandalunch.domain.usecase

import app.cash.turbine.test
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.repository.CategoryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GetCategoriesUseCaseTest {

    private val repository: CategoryRepository = mockk(relaxed = true)
    private val useCase = GetCategoriesUseCase(repository)

    @Test
    fun invoke_repositoryEmitsCategories_emitsSameList() = runTest {
        val expected = listOf(
            Category(id = 1, name = "한식", emoji = "🍚", position = 0),
            Category(id = 2, name = "중식", emoji = "🥢", position = 1)
        )
        every { repository.getCategories() } returns flowOf(expected)

        useCase().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun invoke_repositoryEmitsEmptyList_emitsEmptyList() = runTest {
        every { repository.getCategories() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(emptyList<Category>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun invoke_callsRepositoryGetCategoriesExactlyOnce() = runTest {
        every { repository.getCategories() } returns flowOf(emptyList())

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        verify(exactly = 1) { repository.getCategories() }
    }
}
