package com.example.mandalunch.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * `Dispatchers.Main`을 `TestDispatcher`로 치환하는 공용 JUnit Rule.
 *
 * ViewModel 테스트에서 `viewModelScope`(기본 `Dispatchers.Main.immediate`)가
 * 테스트 스레드에서 결정적으로 동작하도록 한다. 가상 시간을 통해
 * `delay()`를 건너뛸 수 있다(`advanceUntilIdle()`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
