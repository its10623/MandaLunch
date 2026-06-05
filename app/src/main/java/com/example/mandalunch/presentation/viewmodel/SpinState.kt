package com.example.mandalunch.presentation.viewmodel

/**
 * 화면 1 (MandalartScreen) 스핀 상태.
 * categoryIndex 는 CW_ORDER(시계방향) 인덱스 (0..7).
 */
sealed class SpinState {
    object Idle : SpinState()
    object Spinning : SpinState()
    data class Selected(val categoryIndex: Int) : SpinState()
}

/**
 * 화면 2 (MenuSelectScreen) 스핀 상태.
 * menuIndex 는 menus 리스트의 인덱스 (0..7).
 */
sealed class MenuSpinState {
    object Idle : MenuSpinState()
    object Spinning : MenuSpinState()
    data class Selected(val menuIndex: Int) : MenuSpinState()
}
