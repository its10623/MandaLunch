package com.example.mandalunch.presentation.util

import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

class RestaurantDisplayFormatterTest {

    /**
     * `String.format` 로케일 의존성을 회피하기 위해 한국 로케일로 고정한다.
     * 영문 로케일에서는 소수점이 콤마(`,`)로 표시될 수 있다.
     */
    @Before
    fun setLocale() {
        Locale.setDefault(Locale.KOREA)
    }

    // ----- cleanCategory -----

    @Test
    fun cleanCategory_emptyString_returnsEmpty() {
        assertEquals("", cleanCategory(""))
    }

    @Test
    fun cleanCategory_blankWhitespace_returnsEmpty() {
        assertEquals("", cleanCategory("   "))
    }

    @Test
    fun cleanCategory_twoLevelCategory_returnsSecondToken() {
        assertEquals("한식", cleanCategory("음식점 > 한식"))
    }

    @Test
    fun cleanCategory_threeLevelCategory_returnsSecondToken() {
        assertEquals("한식", cleanCategory("음식점 > 한식 > 고기요리"))
    }

    @Test
    fun cleanCategory_noSeparator_returnsAsIs() {
        assertEquals("한식", cleanCategory("한식"))
    }

    @Test
    fun cleanCategory_extraSpaces_trimsAndReturnsSecondToken() {
        assertEquals("한식", cleanCategory("음식점  >  한식  "))
    }

    // ----- formatDistanceWithTime -----

    @Test
    fun formatDistanceWithTime_veryClose50m_returns50mWalk1min() {
        assertEquals("50m · 도보 1분", formatDistanceWithTime(50))
    }

    @Test
    fun formatDistanceWithTime_500m_returns500mWalk7min() {
        // round(500/67) = round(7.46) = 7
        assertEquals("500m · 도보 7분", formatDistanceWithTime(500))
    }

    @Test
    fun formatDistanceWithTime_1000m_returns1_0kmWalk15min() {
        // round(1000/67) = round(14.92) = 15
        assertEquals("1.0km · 도보 15분", formatDistanceWithTime(1000))
    }

    @Test
    fun formatDistanceWithTime_1500m_returns1_5kmWalk22min() {
        // round(1500/67) = round(22.38) = 22
        assertEquals("1.5km · 도보 22분", formatDistanceWithTime(1500))
    }

    @Test
    fun formatDistanceWithTime_1999m_returnsAtBoundary2_0kmWalk30min() {
        // round(1999/67) = round(29.83) = 30
        assertEquals("2.0km · 도보 30분", formatDistanceWithTime(1999))
    }

    @Test
    fun formatDistanceWithTime_2000m_returns2_0kmDrive4min() {
        // round(2000/500) = 4
        assertEquals("2.0km · 차로 4분", formatDistanceWithTime(2000))
    }

    @Test
    fun formatDistanceWithTime_5000m_returns5_0kmDrive10min() {
        // round(5000/500) = 10
        assertEquals("5.0km · 차로 10분", formatDistanceWithTime(5000))
    }

    @Test
    fun formatDistanceWithTime_0m_returns0mWalk1min() {
        // coerceAtLeast(1) 보장
        assertEquals("0m · 도보 1분", formatDistanceWithTime(0))
    }
}
