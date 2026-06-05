package com.example.mandalunch.presentation.share

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.example.mandalunch.domain.model.ShareMessage
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link

/**
 * 카카오 SDK 호출을 전담하는 Launcher object.
 *
 * - 카카오톡 설치 시: `ShareClient.shareDefault()`로 인앱 공유 다이얼로그 실행.
 * - 미설치 시: `WebSharerClient.makeDefaultUrl()`로 웹 공유 URL을 만들어
 *   Chrome Custom Tabs(우선) 또는 일반 브라우저(폴백)로 띄운다.
 *
 * 모든 `com.kakao.sdk.*` import는 본 파일에 한정한다.
 */
object KakaoShareLauncher {

    private const val TAG = "KakaoShareLauncher"

    // imageUrl이 null일 때 사용할 기본 이미지 URL (카카오 정책상 https URL 권장)
    private const val DEFAULT_IMAGE_URL =
        "https://play-lh.googleusercontent.com/default_icon"

    fun share(activity: Activity, message: ShareMessage) {
        val template = buildFeedTemplate(message)

        if (ShareClient.instance.isKakaoTalkSharingAvailable(activity)) {
            ShareClient.instance.shareDefault(activity, template) { result, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡 공유 실패", error)
                    fallbackToWebSharer(activity, template)
                } else if (result != null) {
                    activity.startActivity(result.intent)
                }
            }
        } else {
            fallbackToWebSharer(activity, template)
        }
    }

    private fun buildFeedTemplate(message: ShareMessage): FeedTemplate {
        val imageUrl = message.imageUrl ?: DEFAULT_IMAGE_URL
        return FeedTemplate(
            content = Content(
                title = message.title,
                description = message.description,
                imageUrl = imageUrl,
                link = Link(
                    webUrl = message.linkWebUrl,
                    mobileWebUrl = message.linkMobileWebUrl
                )
            ),
            buttons = listOf(
                Button(
                    title = message.buttonTitle,
                    link = Link(
                        webUrl = message.linkWebUrl,
                        mobileWebUrl = message.linkMobileWebUrl
                    )
                )
            )
        )
    }

    private fun fallbackToWebSharer(activity: Activity, template: FeedTemplate) {
        try {
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(template)
            try {
                CustomTabsIntent.Builder().build().launchUrl(activity, sharerUrl)
            } catch (_: Exception) {
                val intent = Intent(Intent.ACTION_VIEW, sharerUrl).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "웹 공유 fallback 실패", e)
        }
    }
}
