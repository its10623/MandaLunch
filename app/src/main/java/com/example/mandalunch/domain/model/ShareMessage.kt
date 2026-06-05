package com.example.mandalunch.domain.model

/**
 * 카카오 피드 메시지 페이로드를 추상화한 도메인 모델.
 *
 * Presentation 레이어가 외부 SDK(Kakao)에 직접 의존하지 않도록 중간 표현으로 사용된다.
 * 실제 SDK 매핑은 [com.example.mandalunch.presentation.share.KakaoShareLauncher]에서 수행한다.
 *
 * @property title 메시지 카드 상단 제목
 * @property description 본문
 * @property imageUrl 메시지 카드 이미지 URL. null이면 Launcher가 기본 이미지 URL 사용.
 * @property linkWebUrl PC 웹 브라우저 이동 URL (카카오 디벨로퍼스에 등록된 도메인이어야 함)
 * @property linkMobileWebUrl 모바일 웹 이동 URL
 * @property buttonTitle 카드 하단 버튼 라벨
 */
data class ShareMessage(
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val linkWebUrl: String = "https://play.google.com/store/apps/details?id=com.example.mandalunch",
    val linkMobileWebUrl: String = "https://play.google.com/store/apps/details?id=com.example.mandalunch",
    val buttonTitle: String = "MandaLunch 앱 보기"
)
