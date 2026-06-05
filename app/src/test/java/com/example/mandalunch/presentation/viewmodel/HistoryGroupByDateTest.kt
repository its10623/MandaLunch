package com.example.mandalunch.presentation.viewmodel

import com.example.mandalunch.domain.model.RecommendHistory
import com.example.mandalunch.domain.usecase.DeleteAllHistoryUseCase
import com.example.mandalunch.domain.usecase.GetHistoryUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `HistoryViewModel.groupByDate` 순수 함수 테스트.
 *
 * `groupByDate`는 `@VisibleForTesting internal` 이므로 동일 모듈에서 접근 가능하다.
 * Dispatcher 불필요 — 순수 함수이므로 init 블록만 비활성화하면 된다.
 */
class HistoryGroupByDateTest {

    private val zone: ZoneId = ZoneId.of("Asia/Seoul")
    // 고정 기준 시각: 2026-06-05 14:30 KST (금요일)
    private val now: Long = LocalDateTime.of(2026, 6, 5, 14, 30)
        .atZone(zone).toInstant().toEpochMilli()

    private fun createViewModel(): HistoryViewModel {
        val getHistoryUseCase: GetHistoryUseCase = mockk(relaxed = true)
        val deleteAllHistoryUseCase: DeleteAllHistoryUseCase = mockk(relaxed = true)
        every { getHistoryUseCase() } returns flowOf(emptyList())
        return HistoryViewModel(getHistoryUseCase, deleteAllHistoryUseCase)
    }

    private fun epoch(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        LocalDateTime.of(year, month, day, hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun history(at: Long, id: Int = 0, menu: String = "비빔밥"): RecommendHistory =
        RecommendHistory(
            id = id,
            menuId = 0,
            menuName = menu,
            categoryName = "한식",
            recommendedAt = at
        )

    @Test
    fun groupByDate_emptyInput_returnsEmptyList() {
        val vm = createViewModel()
        val result = vm.groupByDate(emptyList(), now, zone)
        assertEquals(emptyList(), result)
    }

    @Test
    fun groupByDate_todayHistory_returnsTodaySection() {
        val vm = createViewModel()
        val histories = listOf(history(epoch(2026, 6, 5, 12, 0)))

        val result = vm.groupByDate(histories, now, zone)

        assertEquals(1, result.size)
        assertEquals(DateGroup.TODAY, result[0].group)
        assertEquals(1, result[0].items.size)
        assertEquals("12:00", result[0].items[0].timeLabel)
    }

    @Test
    fun groupByDate_yesterdayHistory_returnsYesterdaySection() {
        val vm = createViewModel()
        val histories = listOf(history(epoch(2026, 6, 4, 9, 0)))

        val result = vm.groupByDate(histories, now, zone)

        assertEquals(1, result.size)
        assertEquals(DateGroup.YESTERDAY, result[0].group)
        assertEquals(1, result[0].items.size)
    }

    @Test
    fun groupByDate_thisWeekHistory_returnsThisWeekSection() {
        // 2026-06-01 (월요일) 18:00. now가 2026-06-05(금요일)이므로 이번 주.
        val vm = createViewModel()
        val histories = listOf(history(epoch(2026, 6, 1, 18, 0)))

        val result = vm.groupByDate(histories, now, zone)

        assertEquals(1, result.size)
        assertEquals(DateGroup.THIS_WEEK, result[0].group)
        assertEquals(1, result[0].items.size)
    }

    @Test
    fun groupByDate_earlierHistory_returnsEarlierSection() {
        val vm = createViewModel()
        val histories = listOf(history(epoch(2026, 5, 20, 12, 0)))

        val result = vm.groupByDate(histories, now, zone)

        assertEquals(1, result.size)
        assertEquals(DateGroup.EARLIER, result[0].group)
    }

    @Test
    fun groupByDate_mixedSections_returnsInOrder_TODAY_YESTERDAY_THISWEEK_EARLIER() {
        val vm = createViewModel()
        val histories = listOf(
            history(epoch(2026, 5, 20, 12, 0), id = 4),   // EARLIER
            history(epoch(2026, 6, 4, 9, 0), id = 2),     // YESTERDAY
            history(epoch(2026, 6, 1, 18, 0), id = 3),    // THIS_WEEK (월요일)
            history(epoch(2026, 6, 5, 12, 0), id = 1)     // TODAY
        )

        val result = vm.groupByDate(histories, now, zone)

        assertEquals(
            listOf(DateGroup.TODAY, DateGroup.YESTERDAY, DateGroup.THIS_WEEK, DateGroup.EARLIER),
            result.map { it.group }
        )
    }

    @Test
    fun groupByDate_withinTodayMultipleItems_sortsByRecommendedAtDescending() {
        val vm = createViewModel()
        val histories = listOf(
            history(epoch(2026, 6, 5, 9, 0), id = 1, menu = "A"),
            history(epoch(2026, 6, 5, 14, 0), id = 2, menu = "B"),
            history(epoch(2026, 6, 5, 11, 0), id = 3, menu = "C")
        )

        val result = vm.groupByDate(histories, now, zone)

        assertEquals(1, result.size)
        assertEquals(DateGroup.TODAY, result[0].group)
        assertEquals(
            listOf("14:00", "11:00", "09:00"),
            result[0].items.map { it.timeLabel }
        )
    }

    @Test
    fun groupByDate_timeLabel_isFormattedAsHHmmWithZeroPadding() {
        val vm = createViewModel()
        val histories = listOf(history(epoch(2026, 6, 5, 14, 5)))

        val result = vm.groupByDate(histories, now, zone)

        assertEquals(1, result.size)
        assertEquals("14:05", result[0].items[0].timeLabel)
    }

    @Test
    fun groupByDate_mondayBoundary_groupsCorrectly() {
        // now가 월요일일 때 경계 검증.
        // 그룹 우선순위: TODAY > YESTERDAY > THIS_WEEK > EARLIER
        //  - 2026-06-08(월) 00:01 → TODAY (today와 동일 LocalDate)
        //  - 2026-06-07(일) 23:59 → YESTERDAY (today.minusDays(1)와 동일 LocalDate)
        //  - 2026-06-06(토) → EARLIER (이번주 월요일=06-08보다 이전, yesterday도 아님)
        val mondayNow = LocalDateTime.of(2026, 6, 8, 14, 30)
            .atZone(zone).toInstant().toEpochMilli()

        val vm = createViewModel()
        val histories = listOf(
            history(epoch(2026, 6, 8, 0, 1), id = 1),    // 월요일 00:01 → TODAY
            history(epoch(2026, 6, 7, 23, 59), id = 2),  // 일요일 23:59 → YESTERDAY
            history(epoch(2026, 6, 6, 12, 0), id = 3)    // 토요일 → EARLIER
        )

        val result = vm.groupByDate(histories, mondayNow, zone)

        val groups = result.map { it.group }
        assertTrue(groups.contains(DateGroup.TODAY), "TODAY section must exist")
        assertTrue(groups.contains(DateGroup.YESTERDAY), "YESTERDAY section must exist for Sunday (= today-1)")
        assertTrue(groups.contains(DateGroup.EARLIER), "EARLIER section must exist for Saturday")

        // 월요일 새벽은 TODAY (자정 직후)
        val todaySection = result.first { it.group == DateGroup.TODAY }
        assertEquals(1, todaySection.items.size)

        // 일요일은 YESTERDAY (today.minusDays(1))
        val yesterdaySection = result.first { it.group == DateGroup.YESTERDAY }
        assertEquals(1, yesterdaySection.items.size)

        // 토요일은 EARLIER (이번주 월요일 6-8보다 이전, yesterday 아님)
        val earlierSection = result.first { it.group == DateGroup.EARLIER }
        assertEquals(1, earlierSection.items.size)
    }
}
