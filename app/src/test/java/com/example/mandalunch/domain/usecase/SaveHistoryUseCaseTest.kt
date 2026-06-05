package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.RecommendHistory
import com.example.mandalunch.domain.repository.RecommendHistoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class SaveHistoryUseCaseTest {

    private val repository: RecommendHistoryRepository = mockk(relaxed = true)
    private val useCase = SaveHistoryUseCase(repository)

    private val sampleHistory = RecommendHistory(
        id = 0,
        menuId = 1,
        menuName = "비빔밥",
        categoryName = "한식",
        recommendedAt = 1_700_000_000_000L
    )

    @Test
    fun invoke_validHistory_callsRepositorySaveHistoryOnce() = runTest {
        useCase(sampleHistory)

        coVerify(exactly = 1) { repository.saveHistory(sampleHistory) }
    }

    @Test
    fun invoke_repositoryThrows_propagatesException() = runTest {
        coEvery { repository.saveHistory(any()) } throws IOException("disk full")

        assertFailsWith<IOException> {
            useCase(sampleHistory)
        }
    }
}
