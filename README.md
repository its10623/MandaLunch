# MandaLunch 🍱

> 9×9 만다라트 그리드로 오늘 점심 메뉴를 결정해주는 Android 앱

## 기능 소개

### 🎲 만다라트 메뉴 추천
9×9 만다라트 그리드에서 카테고리를 선택하면 3×3 메뉴 그리드가 원형 순환 스핀으로 랜덤하게 오늘의 메뉴를 결정합니다.

### 🗺️ 근처 음식점 검색
추천된 메뉴 기준으로 현재 위치 반경 3km 음식점을 검색합니다. 메뉴명 검색 → 카테고리 검색 2단계 폴백으로 결과를 보장합니다.

### 🕐 추천 히스토리
오늘 / 어제 / 이번 주 / 이전으로 날짜별 그룹화된 추천 기록을 관리합니다.

### ♥ 즐겨찾기
자주 먹는 메뉴를 즐겨찾기에 추가하면 메뉴 선택 화면 상단에 우선 배치됩니다.

## 기술 스택

| 분류 | 사용 기술 |
|---|---|
| 언어 | Kotlin |
| UI | Jetpack Compose + Material3 |
| 아키텍처 | Clean Architecture + MVVM |
| DI | Hilt |
| DB | Room |
| 네트워크 | Retrofit2 + OkHttp3 |
| 위치 | FusedLocationProviderClient |
| 외부 API | Kakao Local API |
| 테스트 | JUnit4 + MockK + Turbine |

## 아키텍처

- **단방향 의존성** — Domain은 Android 프레임워크에 의존하지 않는 순수 Kotlin
- **UiState + UiEvent 패턴** — StateFlow로 화면 상태, SharedFlow로 1회성 이벤트 처리
- **단위 테스트 74개** — UseCase / ViewModel / 유틸 함수 전 범위 커버

## 📁 프로젝트 구조

```
com.example.mandalunch
│
├── data
│   ├── local
│   │   └── room
│   │       ├── dao
│   │       │   ├── CategoryDao
│   │       │   ├── MenuDao
│   │       │   └── RecommendHistoryDao
│   │       ├── entity
│   │       │   ├── CategoryEntity
│   │       │   ├── MenuEntity
│   │       │   └── RecommendHistoryEntity
│   │       └── MandaLunchDatabase
│   ├── remote
│   │   ├── dto
│   │   │   └── KakaoPlaceDto
│   │   ├── FusedLocationDataSource
│   │   └── KakaoLocalApiService
│   └── repository
│       ├── CategoryRepositoryImpl
│       ├── LocationRepositoryImpl
│       ├── MenuRepositoryImpl
│       ├── RecommendHistoryRepositoryImpl
│       └── RestaurantRepositoryImpl
│
├── di
│   ├── DatabaseModule
│   ├── LocationModule
│   ├── NetworkModule
│   └── RepositoryModule
│
├── domain
│   ├── model
│   │   ├── Category
│   │   ├── Coordinates
│   │   ├── Menu
│   │   ├── RecommendHistory
│   │   └── Restaurant
│   ├── repository
│   │   ├── CategoryRepository
│   │   ├── LocationRepository
│   │   ├── MenuRepository
│   │   ├── RecommendHistoryRepository
│   │   └── RestaurantRepository
│   └── usecase
│       ├── ClearAllFavoritesUseCase
│       ├── DeleteAllHistoryUseCase
│       ├── GetCategoriesUseCase
│       ├── GetCurrentLocationUseCase
│       ├── GetFavoritesUseCase
│       ├── GetHistoryUseCase
│       ├── GetMenusByCategoryUseCase
│       ├── SaveHistoryUseCase
│       ├── SearchNearbyRestaurantsUseCase
│       └── ToggleFavoriteUseCase
│
└── presentation
    ├── navigation
    │   └── NavGraph
    ├── ui
    │   ├── component
    │   │   ├── CategoryBlock
    │   │   ├── GridCell
    │   │   ├── SpinBlock
    │   │   └── SpinButton
    │   ├── screen
    │   │   ├── FavoriteScreen
    │   │   ├── HistoryScreen
    │   │   ├── MandalartScreen
    │   │   ├── MenuSelectScreen
    │   │   ├── RestaurantScreen
    │   │   └── ResultScreen
    │   └── theme
    │       ├── Color
    │       ├── Theme
    │       └── Type
    ├── util
    │   └── RestaurantDisplayFormatter
    └── viewmodel
        ├── FavoriteViewModel
        ├── HistoryViewModel
        ├── MandalartViewModel
        ├── MenuSelectViewModel
        ├── RestaurantViewModel
        ├── ResultViewModel
        └── SpinState
```

## 카테고리

한식 · 중식 · 일식 · 양식 · 아시안 · 분식 · 프랜차이즈 · 건강식
