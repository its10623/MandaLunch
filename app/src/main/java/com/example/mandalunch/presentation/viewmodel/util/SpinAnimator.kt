package com.example.mandalunch.presentation.viewmodel.util

import kotlinx.coroutines.delay

object SpinAnimator {

    private const val SLOTS = 8

    suspend fun run(
        targetIndex: Int,
        rounds: Int = 3,
        onHighlight: (Int) -> Unit,
        onComplete: (Int) -> Unit
    ) {
        val totalSteps = rounds * SLOTS + targetIndex + 1
        for (step in 0 until totalSteps) {
            onHighlight(step % SLOTS)
            delay(delayFor(step, totalSteps))
        }
        onComplete(targetIndex)
    }

    private fun delayFor(step: Int, total: Int): Long {
        val ratio = step.toFloat() / total
        return when {
            ratio < 0.50f -> 70L
            ratio < 0.72f -> 120L
            ratio < 0.88f -> 200L
            ratio < 0.96f -> 320L
            else          -> 480L
        }
    }
}
