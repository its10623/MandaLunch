package com.example.mandalunch.presentation.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Compose 내 [androidx.compose.ui.platform.LocalContext] 등으로 얻은 Context에서
 * 최상위 [Activity]를 찾아 반환한다.
 *
 * Kakao SDK의 `ShareClient`는 Activity Context가 필요하기 때문에,
 * `ContextWrapper` 체인을 거슬러 올라가 실제 Activity를 확보한다.
 *
 * @throws IllegalStateException context 체인에 Activity가 없는 경우
 */
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    error("No Activity found in context chain")
}
