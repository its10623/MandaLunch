package com.example.mandalunch.domain.usecase

import com.example.mandalunch.domain.model.Coordinates
import com.example.mandalunch.domain.model.Restaurant
import com.example.mandalunch.domain.repository.RestaurantRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchNearbyRestaurantsUseCaseTest {

    private val repository: RestaurantRepository = mockk(relaxed = true)
    private val useCase = SearchNearbyRestaurantsUseCase(repository)

    private val coords = Coordinates(latitude = 37.5665, longitude = 126.9780)

    private val sampleRestaurant = Restaurant(
        placeName = "맛집",
        categoryName = "음식점 > 한식",
        addressName = "서울시 중구 명동",
        roadAddressName = "서울시 중구 명동길 1",
        phone = "02-1234-5678",
        distanceMeters = 250,
        placeUrl = "http://place.map.kakao.com/1",
        latitude = 37.5665,
        longitude = 126.9780
    )

    @Test
    fun invoke_repositoryReturnsList_returnsSuccessWithSameList() = runTest {
        val expected = listOf(sampleRestaurant)
        coEvery {
            repository.searchNearby(
                query = "비빔밥 음식점",
                coords = coords,
                radiusMeters = 3000,
                size = 15
            )
        } returns expected

        val result = useCase("비빔밥 음식점", coords)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun invoke_repositoryThrowsIOException_returnsFailureWithIOException() = runTest {
        coEvery {
            repository.searchNearby(any(), any(), any(), any())
        } throws IOException("network fail")

        val result = useCase("비빔밥", coords)

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is IOException)
        assertNull(result.getOrNull())
    }

    @Test
    fun invoke_passesRadius3000AndSize15ToRepository() = runTest {
        coEvery { repository.searchNearby(any(), any(), any(), any()) } returns emptyList()

        useCase("김치찌개", coords)

        coVerify(exactly = 1) {
            repository.searchNearby(
                query = "김치찌개",
                coords = coords,
                radiusMeters = 3000,
                size = 15
            )
        }
    }
}
