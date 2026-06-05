package com.example.mandalunch.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.mandalunch.domain.model.Category
import com.example.mandalunch.domain.usecase.GetCategoriesUseCase
import com.example.mandalunch.presentation.navigation.Routes
import com.example.mandalunch.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.net.URLDecoder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCategoriesUseCase: GetCategoriesUseCase = mockk(relaxed = true)

    private val categories = listOf(
        Category(id = 1, name = "한식", emoji = "🍚", position = 0),
        Category(id = 2, name = "중식", emoji = "🥢", position = 1)
    )

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.decode(any()) } answers {
            URLDecoder.decode(firstArg<String>(), "UTF-8")
        }
        every { getCategoriesUseCase() } returns flowOf(categories)
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    private fun makeSavedState(
        categoryId: Int = 1,
        menuName: String = "비빔밥"
    ): SavedStateHandle = SavedStateHandle(
        mapOf(
            Routes.ARG_CATEGORY_ID to categoryId,
            Routes.ARG_MENU_NAME to menuName
        )
    )

    private fun makeViewModel(categoryId: Int = 1, menuName: String = "비빔밥") =
        ResultViewModel(makeSavedState(categoryId, menuName), getCategoriesUseCase)

    @Test
    fun init_validArgs_loadsCategoryAndMenuName() = runTest {
        val viewModel = makeViewModel(categoryId = 1, menuName = "비빔밥")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.category?.id)
        assertEquals("비빔밥", viewModel.uiState.value.menuName)
    }

    @Test
    fun init_urlEncodedMenuName_decodesCorrectly() = runTest {
        val encoded = "%EB%B9%84%EB%B9%94%EB%B0%A5"
        val viewModel = makeViewModel(categoryId = 1, menuName = encoded)
        advanceUntilIdle()

        assertEquals("비빔밥", viewModel.uiState.value.menuName)
    }

    @Test
    fun init_unknownCategoryId_categoryIsNull() = runTest {
        val viewModel = makeViewModel(categoryId = -1)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.category)
    }

    @Test
    fun onRestartClick_emitsNavigateBackToMandalart() = runTest {
        val viewModel = makeViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onRestartClick()
            advanceUntilIdle()
            assertEquals(ResultUiEvent.NavigateBackToMandalart, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onSameCategoryAgainClick_emitsNavigateBackToMenuSelect() = runTest {
        val viewModel = makeViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onSameCategoryAgainClick()
            advanceUntilIdle()
            assertEquals(ResultUiEvent.NavigateBackToMenuSelect, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onFindRestaurantsClick_categoryFound_emitsWithCategoryName() = runTest {
        val viewModel = makeViewModel(categoryId = 1, menuName = "비빔밥")
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onFindRestaurantsClick()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is ResultUiEvent.NavigateToRestaurant)
            assertEquals("비빔밥", event.menuName)
            assertEquals("한식", event.categoryName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onFindRestaurantsClick_categoryNotFound_fallsBackToMenuName() = runTest {
        val viewModel = makeViewModel(categoryId = -1, menuName = "비빔밥")
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onFindRestaurantsClick()
            advanceUntilIdle()
            val event = awaitItem()
            assertTrue(event is ResultUiEvent.NavigateToRestaurant)
            assertEquals("비빔밥", event.menuName)
            assertEquals("비빔밥", event.categoryName)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
