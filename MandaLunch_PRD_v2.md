# PRD: 점심메뉴 추천 앱 (MandaLunch) — v2.3

> **변경 이력**
> | 버전 | 날짜 | 주요 변경 |
> |------|------|-----------|
> | v1.0 | 최초 작성 | 초기 PRD |
> | v2.0 | 업데이트 | UI 프레임워크 → Jetpack Compose, 레이아웃 → 9×9 만다라트(81칸), 스핀 애니메이션 원형 순환 방식으로 변경 |
> | v2.1 | 2026-06-04 | 위치 기반 음식점 추천 기능(RestaurantScreen) 추가, Retrofit/OkHttp/play-services-location 의존성 추가, Kakao Local API 연동 |
> | v2.2 | 2026-06-05 | 추천 히스토리 기능(HistoryScreen) 추가, GetHistoryUseCase/DeleteAllHistoryUseCase 신규, MandalartScreen 히스토리 진입 버튼, ResultViewModel 자동 저장(SavedStateHandle 중복 방지) |
> | v2.3 | 2026-06-05 | 즐겨찾기 기능(FavoriteScreen) 추가, ToggleFavoriteUseCase/GetFavoritesUseCase/ClearAllFavoritesUseCase 신규, MenuSelectScreen 즐겨찾기 우선 정렬 + 하트 토글, MandalartScreen 헤더 ♥ 진입 버튼 |
> | v2.3 (patch) | 2026-06-05 | 단위 테스트 77건 도입(UseCase 13 + ViewModel 41 + 유틸/groupByDate 23) + 테스트 가능성 리팩토링(`presentation/util/RestaurantDisplayFormatter.kt` 추출, `groupByDate` `@VisibleForTesting internal`, `randomProvider` 주입) — 동작 변경 없음 |

---

## 1. 개요 (Overview)

| 항목 | 내용 |
|------|------|
| 앱 이름 | MandaLunch |
| 플랫폼 | Android |
| 개발 언어 | Kotlin |
| UI 프레임워크 | **Jetpack Compose** |
| 최소 SDK | API 26 (Android 8.0) |
| 아키텍처 | MVVM + Clean Architecture |

---

## 2. 목적 및 배경

점심 메뉴 결정에 어려움을 겪는 사용자를 위해, 오타니 쇼헤이의 **만다라트 차트(9×9 = 81칸)** 구조를 활용한 2단계 랜덤 선택 앱을 제공한다.

- **1단계**: 9×9 전체 그리드에서 카테고리 원형 순환 스핀으로 카테고리 선택
- **2단계**: 3×3 메뉴 그리드에서 원형 순환 스핀으로 최종 메뉴 선택

---

## 3. 핵심 플로우

```
앱 실행
  └─► [화면 1] 9×9 만다라트 — 카테고리 선택
          ├─► 헤더 즐겨찾기 버튼(♥) 탭 → [화면 6] FavoriteScreen (카테고리별 즐겨찾기 조회/해제)
          ├─► 헤더 히스토리 버튼(🕐) 탭 → [화면 5] HistoryScreen (추천 이력 조회)
          └─► 가운데 SPIN 버튼 탭
                └─► 애니메이션: 8개 카테고리 원형 순환 후 1개 선택
                      └─► "○○ 메뉴 보기" 버튼 활성화
                            └─► [화면 2] 3×3 메뉴 그리드 (즐겨찾기 우선 정렬)
                                    │     └─► 메뉴 셀 우상단 ♥ 탭 → 즐겨찾기 토글 (Idle 상태에서만)
                                    └─► 가운데 탭 또는 "메뉴 뽑기" 버튼 탭
                                          └─► 애니메이션: 8개 메뉴 원형 순환 후 1개 선택
                                                └─► [화면 3] 결과 화면
                                                      │     (진입 시 추천 이력 자동 저장)
                                                      └─► "🗺️ 근처 음식점 찾기" 탭
                                                            └─► [화면 4] RestaurantScreen
                                                                  (Kakao Local API 주변 1km 음식점)
```

---

## 4. 화면별 상세 명세

### 4-1. 화면 1 — 9×9 만다라트 (카테고리 선택)

#### 레이아웃 구조

전체 81칸을 9개의 3×3 블록으로 나눈다.

```
┌────────────┬────────────┬────────────┐
│  [한식 블록]  │  [중식 블록]  │  [일식 블록]  │
│ 8메뉴+한식명 │ 8메뉴+중식명 │ 8메뉴+일식명 │
├────────────┼────────────┼────────────┤
│  [양식 블록]  │  [CENTER]  │ [아시안 블록] │
│ 8메뉴+양식명 │  방향힌트+  │ 8메뉴+아시안 │
│            │  🎲 SPIN   │            │
├────────────┼────────────┼────────────┤
│  [분식 블록]  │[프랜차이즈]  │ [건강식 블록] │
│ 8메뉴+분식명 │ 8메뉴+프랜명 │ 8메뉴+건강식 │
└────────────┴────────────┴────────────┘
```

#### 각 3×3 블록 내부 구조

```
┌──────┬──────┬──────┐
│메뉴 1 │메뉴 2 │메뉴 3 │
├──────┼──────┼──────┤
│메뉴 8 │카테명 │메뉴 4 │  ← 가운데 = 카테고리 셀 (강조)
├──────┼──────┼──────┤
│메뉴 7 │메뉴 6 │메뉴 5 │
└──────┴──────┴──────┘
```

#### 가운데(CENTER) 3×3 블록 내부 구조

```
┌──────┬──────┬──────┐
│↖ 한식 │↑ 중식 │↗ 일식 │
├──────┼──────┼──────┤
│← 양식 │ 🎲   │→아시안│  ← 가운데 = 메인 SPIN 버튼
├──────┼──────┼──────┤
│↙ 분식 │↓프랜차│↘건강식│
└──────┴──────┴──────┘
```

#### 셀 타입 및 스타일

| 셀 타입 | 설명 | 배경 | 텍스트 |
|---------|------|------|--------|
| `MenuCell` | 일반 메뉴 표시 | surface2 | text |
| `CategoryCell` | 각 블록 중앙, 카테고리명 | 빨강 그라디언트 | white, bold |
| `CenterSpinCell` | 전체 가운데, SPIN 버튼 | 주황 그라디언트 | white, bold |
| `HintCell` | CENTER 블록 주변 8칸 | 반투명 | dim |
| `GlowCell` | 스핀 중 하이라이트 | 초록↔파랑 그라디언트 | white |
| `SelectedCell` | 최종 선택된 카테고리 | 빨강 그라디언트 + glow | white, bold |

#### 블록 경계선

3×3 블록 사이 경계: `2dp` 반투명 흰색 구분선 (Compose `Divider` 또는 `Border` modifier)

#### 스핀 애니메이션 — 원형 순환

시계방향 순서로 8개 카테고리 셀을 하이라이트가 순환하며 돌다가 감속 후 선택.

```
시계방향 순서:
한식(↖) → 중식(↑) → 일식(↗) → 아시안(→) → 건강식(↘) → 프랜차이즈(↓) → 분식(↙) → 양식(←) → 반복
```

**애니메이션 타이밍 스펙:**

| 구간 | 진행률 | 딜레이(ms) |
|------|--------|-----------|
| 빠름 | 0 ~ 50% | 70 |
| 중간1 | 50 ~ 72% | 120 |
| 중간2 | 72 ~ 88% | 200 |
| 느림 | 88 ~ 96% | 320 |
| 최종 | 96 ~ 100% | 480 |

- 총 스텝: `3바퀴(24) + targetIndex + 1`
- 선택 완료 시: `Scale 1.0 → 1.1` pulse + 햅틱 진동

#### 완료 후 버튼 동작

| 상태 | 버튼 텍스트 | 색상 | 동작 |
|------|------------|------|------|
| 미선택 | 🎲 랜덤 뽑기 | 빨강→주황 그라디언트 | startSpin() |
| 선택 완료 | 🍽️ ○○ 메뉴 보기 | 주황→오렌지 그라디언트 | 화면 2 이동 |

---

### 4-2. 화면 2 — 3×3 메뉴 그리드 (메뉴 선택)

#### 레이아웃 구조

```
┌───────┬───────┬───────┐
│메뉴 1  │메뉴 2  │메뉴 3  │  (96dp × 96dp 셀)
├───────┼───────┼───────┤
│메뉴 8  │[카테고] │메뉴 4  │
├───────┼───────┼───────┤
│메뉴 7  │메뉴 6  │메뉴 5  │
└───────┴───────┴───────┘
```

- 가운데(2,2): 선택된 **카테고리 이름 + 이모지** — 탭 시 스핀 시작
- 나머지 8칸: 시계방향 순서로 메뉴 배치

#### 스핀 애니메이션 — 원형 순환

메뉴 8개도 동일한 시계방향 원형 순환 방식 적용 (타이밍 스펙 동일).

#### 완료 후 버튼 동작

| 상태 | 버튼 텍스트 | 색상 | 동작 |
|------|------------|------|------|
| 미선택 | 🎲 메뉴 뽑기 | 빨강→주황 그라디언트 | startMenuSpin() |
| 선택 완료 | 🎉 결과 보기 | 주황→오렌지 그라디언트 | 화면 3 이동 |

#### 상단 바

- 좌: `← 돌아가기` 버튼 (화면 1로)
- 우: 선택된 카테고리 배지 (이모지 + 이름, 빨강 pill)

---

### 4-3. 화면 3 — 결과 화면

| 구성 요소 | 설명 |
|----------|------|
| 카테고리 이모지 | 크게 표시 (4rem) |
| 카테고리 배지 | 빨강 pill |
| 메뉴 이름 | 주황 그라디언트 대형 텍스트 |
| 서브 텍스트 | "오늘의 점심은 이걸로 정해졌습니다! 🎉" |
| 버튼 1 | 🔄 처음부터 다시 → 화면 1 초기화 |
| 버튼 2 | 🔀 같은 카테고리에서 다시 → 화면 2 재진입 |
| 버튼 3 (v2.1) | 🗺️ 근처 음식점 찾기 → 화면 4(RestaurantScreen) 이동 |

---

### 4-4. 화면 4 — RestaurantScreen (v2.1 신규)

**목적:** 결과 화면에서 선택된 메뉴를 검색 키워드로 사용해, 사용자 현재 위치(FusedLocationProviderClient) 주변 1km 이내의 음식점을 Kakao Local API로 조회하여 카드 목록으로 보여준다.

**경로:** `Routes.RESTAURANT/{menuName}` (Uri.encode/Uri.decode 라운드트립)

**UiState (sealed class `RestaurantUiState`):**

| 상태 | 트리거 | 화면 표현 |
|------|--------|-----------|
| `Loading` | 진입 직후 위치 획득 + API 호출 진행 | CircularProgressIndicator |
| `PermissionDenied` | 위치 권한 거부 | "설정에서 위치 권한을 허용해주세요" 안내 + 재시도 버튼 |
| `Success(menuName, restaurants)` | 검색 결과 수신 | LazyColumn으로 RestaurantCard 목록(빈 결과 시 "주변에 음식점이 없어요" 안내) |
| `Error(message)` | 네트워크/위치 획득 실패 | 에러 메시지 + 재시도 버튼 |

**RestaurantCard 표시 항목:**
- placeName(타이틀) / categoryName(서브) / roadAddressName / distanceMeters("350m") / phone
- 카드 탭 → `Intent.ACTION_VIEW`로 placeUrl(카카오맵) 외부 핸드오프

**비즈니스 규칙:**
- 검색 키워드: `"{menuName} 음식점"` (UseCase 내부에 격리)
- 검색 반경: 1000m (기본), 결과 개수: 15
- Kakao API: `GET https://dapi.kakao.com/v2/local/search/keyword.json`
- 인증: `Authorization: KakaoAK {BuildConfig.KAKAO_REST_API_KEY}`

**권한 처리:**
- `ACCESS_FINE_LOCATION` + `ACCESS_COARSE_LOCATION` 둘 중 하나 이상 허용 시 진행
- Compose Screen이 `rememberLauncherForActivityResult(RequestMultiplePermissions())` 소유
- ViewModel은 `onPermissionResult(granted: Boolean)`만 받아 Android Context 누출 차단

---

### 4-5. 화면 5 — HistoryScreen (v2.2 신규)

**목적:** 지금까지 추천받은 메뉴 이력을 날짜 그룹별로 조회하고 전체 삭제할 수 있다.

**진입 방법:** MandalartScreen(화면 1) 헤더 우측 히스토리 버튼(🕐) 탭

**경로:** `Routes.HISTORY = "history"` (인자 없는 단순 라우트)

**날짜 그룹화 (DateGroup, ViewModel 레이어):**

| 그룹 | 분류 기준 |
|------|----------|
| TODAY (오늘) | 오늘 00:00:00 ~ 현재 |
| YESTERDAY (어제) | 어제 00:00:00 ~ 오늘 00:00:00 |
| THIS_WEEK (이번 주) | 이번 주 월요일 00:00:00 ~ 어제 00:00:00 |
| EARLIER (이전) | 이번 주 월요일보다 이전 |

**UiState (data class `HistoryUiState`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| `sections` | `List<HistorySection>` | 그룹별 정렬된 섹션 리스트 |
| `isLoading` | `Boolean` | 최초 Flow 수신 전 true |
| `isEmpty` | `Boolean` | 전체 히스토리 0건 시 true |
| `showDeleteConfirm` | `Boolean` | 전체 삭제 확인 다이얼로그 표시 여부 |

**4상태 화면:**

| 상태 | 사용자가 보는 것 |
|------|----------------|
| Loading | CircularProgressIndicator 중앙 |
| Empty (isEmpty=true) | "아직 추천 기록이 없어요" placeholder |
| 섹션 목록 | LazyColumn — 날짜 그룹 헤더 + HistoryRow |
| DeleteConfirm | AlertDialog (확인 버튼: AccentRed, 배경: Surface2Dark) |

**HistoryRow 표시 항목:**
- 메뉴명 (TextPrimary, 16sp Bold)
- 카테고리명 (AccentOrange, 12sp) — 빈 문자열이면 비표시
- 시간 라벨 (TextDim, 12sp) — "HH:mm" 형식

**전체 삭제 흐름:**
1. 헤더 전체 삭제 버튼 탭 → `showDeleteConfirm = true`
2. AlertDialog 확인 → `DeleteAllHistoryUseCase()` 실행
3. Room Flow가 빈 리스트 emit → `isEmpty = true`
4. `DeleteAllCompleted` UiEvent emit (선택적 Snackbar 표시용)

**디자인 토큰 적용:**

| 영역 | 색상 |
|------|------|
| 전체 배경 | `BackgroundDark` |
| TopAppBar | `SurfaceDark` |
| 섹션 헤더 | `SurfaceDark` 배경 + `TextDim` 12sp |
| 아이템 카드 | `Surface2Dark` 배경, `RoundedCornerShape(12.dp)` |
| 메뉴명 텍스트 | `TextPrimary` |
| 카테고리 라벨 | `AccentOrange` |
| 시간 라벨 | `TextDim` |
| 전체 삭제 확인 버튼 | `AccentRed` |

**저장 흐름 (ResultScreen 진입 시):**
- `ResultViewModel.init` 에서 category 로딩 완료 후 `SaveHistoryUseCase` 1회 호출
- `SavedStateHandle["history_saved"]` 플래그로 중복 저장 방지
  - 화면 회전: ViewModel 유지 → 자연 방지
  - 프로세스 복원: SavedStateHandle 복원 → 중복 차단
  - 다음 추천: 새 backstack entry = 새 SavedStateHandle → 독립 저장
- `menuId = 0` 저장 (NavGraph가 menuId를 전달하지 않으므로, 표시에 불필요한 필드는 0으로 저장)
- `menuName.isBlank()` 가드로 빈 메뉴명 저장 방어

---

### 4-6. 화면 6 — FavoriteScreen (v2.3 신규)

**목적:** 즐겨찾기로 등록한 메뉴를 카테고리별로 그룹화하여 조회하고, 개별 해제 또는 전체 해제할 수 있다.

**진입 방법:** MandalartScreen(화면 1) 헤더 우측 ♥ 버튼 탭 (히스토리 버튼 좌측에 배치)

**경로:** `Routes.FAVORITES = "favorites"` (인자 없는 단순 라우트)

**카테고리 그룹화 (FavoriteViewModel 레이어):**

| 단계 | 처리 |
|------|------|
| 1 | `combine(GetFavoritesUseCase(), GetCategoriesUseCase())`로 두 Flow 결합 |
| 2 | `favorites.groupBy { it.categoryId }` |
| 3 | `Category.position` 오름차순으로 섹션 정렬 |
| 4 | orphan menu(존재하지 않는 카테고리)는 `mapNotNull`로 제외 |
| 5 | 섹션 내부 메뉴는 name 오름차순 정렬 |

**UiState (data class `FavoriteUiState`):**

| 필드 | 타입 | 설명 |
|------|------|------|
| `sections` | `List<FavoriteCategorySection>` | 카테고리별 섹션 리스트 |
| `isLoading` | `Boolean` | 최초 Flow 수신 전 true |
| `isEmpty` | `Boolean` | 즐겨찾기 0건 시 true |
| `showClearConfirm` | `Boolean` | 전체 해제 확인 다이얼로그 표시 여부 |

**4상태 화면:**

| 상태 | 사용자가 보는 것 |
|------|----------------|
| Loading | CircularProgressIndicator |
| Empty (isEmpty=true) | "아직 즐겨찾기한 메뉴가 없어요 ♥" placeholder |
| 섹션 목록 | LazyColumn — 카테고리 헤더(`{emoji} {name}`) + 메뉴 카드 |
| ClearConfirm | AlertDialog (확인 버튼: AccentRed) |

**메뉴 카드 표시 항목:**
- 메뉴명 (TextPrimary, Bold)
- 우측 하트 아이콘 (`Icons.Filled.Favorite`, AccentRed) — 탭 시 즉시 해제

**MenuSelectScreen 통합 (즐겨찾기 우선 정렬):**
- `MenuSelectViewModel.observeMenus()`: `compareByDescending<Menu> { it.isFavorite }.thenBy { it.id }` 정렬 적용
- `MenuCell` 우상단에 하트 아이콘 오버레이 — 스핀 중에는 비표시
- 스핀 중 토글 방지: **UI 가드**(`spinState is MenuSpinState.Idle`일 때만 아이콘 렌더) + **ViewModel 가드**(`onToggleFavorite()` 진입 시 early return)

**MandalartScreen 헤더 변경:**
- 헤더 Row 배치: `[타이틀 Column] → [♥ AccentRed] → [🕐 히스토리]` 좌→우
- `onNavigateToFavorites: () -> Unit` 콜백 파라미터 신규 추가

**전체 해제 흐름:**
1. TopAppBar 우측 "전체 해제" Text 버튼(AccentRed) 탭 → `showClearConfirm = true`
2. AlertDialog 확인 → `ClearAllFavoritesUseCase()` 실행
3. Room Flow가 빈 리스트 emit → `isEmpty = true`
4. `ClearAllCompleted` UiEvent emit (선택적 Snackbar 확장 지점)

**디자인 토큰 적용:**

| 영역 | 색상 |
|------|------|
| 전체 배경 | `BackgroundDark` |
| TopAppBar | `SurfaceDark` |
| 섹션 헤더 | `TextDim` 12sp SemiBold |
| 메뉴 카드 | `Surface2Dark` 배경, `RoundedCornerShape(12.dp)` |
| 메뉴명 텍스트 | `TextPrimary` |
| 활성 하트 | `AccentRed` |
| 비활성 하트 | `TextDim` |
| 전체 해제 확인 버튼 | `AccentRed` |

---

## 5. 데이터 명세

### 카테고리 & 메뉴

| 카테고리 | 이모지 | 메뉴 8개 |
|---------|--------|---------|
| 한식 | 🍚 | 된장찌개, 김치찌개, 비빔밥, 삼겹살, 순두부찌개, 불고기, 갈비탕, 냉면 |
| 중식 | 🥢 | 짜장면, 짬뽕, 탕수육, 마파두부, 볶음밥, 깐풍기, 유산슬, 딤섬 |
| 일식 | 🍣 | 초밥, 라멘, 돈카츠, 우동, 텐동, 오야코동, 야키토리, 타코야키 |
| 양식 | 🍝 | 파스타, 스테이크, 리조또, 피자, 샌드위치, 크림수프, 뇨키, 샐러드 |
| 아시안 | 🌏 | 쌀국수, 팟타이, 나시고렝, 분짜, 커리, 반미, 똠얌꿍, 마라탕 |
| 분식 | 🥡 | 떡볶이, 순대, 김밥, 튀김, 어묵, 라볶이, 치즈볼, 핫도그 |
| 프랜차이즈 | 🏪 | 버거, 치킨, 피자, 써브웨이, 도시락, 맥도날드, 롯데리아, KFC |
| 건강식 | 🥗 | 닭가슴살, 그린샐러드, 아보카도볼, 두부샐러드, 현미밥, 채소비빔밥, 연어포케, 오트밀 |

### 데이터 모델

```kotlin
data class FoodCategory(
    val id: Int,
    val name: String,
    val emoji: String,
    val menus: List<MenuItem>
)

data class MenuItem(
    val id: Int,
    val name: String,
    val categoryId: Int
)
```

---

## 6. 기술 스택 및 아키텍처

### 기술 스택

| 레이어 | 기술 |
|--------|------|
| **UI** | **Jetpack Compose** |
| 상태 관리 | ViewModel + StateFlow / collectAsStateWithLifecycle |
| 애니메이션 | Compose `animate*AsState`, `LaunchedEffect` + `kotlinx.coroutines.delay` |
| 아키텍처 | MVVM + Clean Architecture |
| DI | Hilt |
| 네비게이션 | Navigation Compose (Single Activity) |
| 햅틱 | `HapticFeedbackConstants.VIRTUAL_KEY` |
| 로컬 데이터 | Room |
| 네트워크 (v2.1) | Retrofit 2.11.0 + OkHttp 4.12.0 + GsonConverter |
| 위치 (v2.1) | play-services-location 21.3.0 (`FusedLocationProviderClient`) |
| 외부 API (v2.1) | Kakao Local API (`/v2/local/search/keyword.json`) |
| API 키 보관 (v2.1) | `local.properties` → `BuildConfig.KAKAO_REST_API_KEY` |

### 패키지 구조

```
com.example.mandalunch/
├── data/
│   ├── local/room/          (Entity, DAO, Database)
│   ├── remote/              (v2.1) KakaoLocalApiService, FusedLocationDataSource, dto/KakaoPlaceDto
│   └── repository/          RepositoryImpl (LocationRepositoryImpl v2.1, RestaurantRepositoryImpl v2.1)
├── domain/
│   ├── model/               Menu, Category, RecommendHistory
│   │                        Coordinates (v2.1), Restaurant (v2.1)
│   ├── repository/          Repository Interface
│   │                        LocationRepository (v2.1), RestaurantRepository (v2.1)
│   └── usecase/             GetMenus, GetCategories, RecommendMenu, SaveHistory
│                            GetHistoryUseCase (v2.2), DeleteAllHistoryUseCase (v2.2)
│                            ToggleFavoriteUseCase (v2.3), GetFavoritesUseCase (v2.3), ClearAllFavoritesUseCase (v2.3)
│                            GetCurrentLocationUseCase (v2.1), SearchNearbyRestaurantsUseCase (v2.1)
├── presentation/
│   ├── viewmodel/           MandalartViewModel, MenuSelectViewModel, ResultViewModel
│   │                        HistoryViewModel (v2.2)  ← DateGroup/HistoryUiState/HistoryUiEvent 포함
│   │                        FavoriteViewModel (v2.3)  ← FavoriteUiState/FavoriteUiEvent 포함
│   │                        RestaurantViewModel (v2.1)
│   ├── ui/
│   │   ├── screen/          MandalartScreen, MenuSelectScreen, ResultScreen
│   │   │                    HistoryScreen (v2.2), FavoriteScreen (v2.3), RestaurantScreen (v2.1)
│   │   ├── component/       GridCell, CategoryBlock, SpinBlock, SpinButton
│   │   └── theme/           Color.kt, Typography.kt, Theme.kt
│   ├── util/                RestaurantDisplayFormatter (v2.3-patch)  ← cleanCategory / formatDistanceWithTime (Compose 비의존, internal)
│   └── navigation/          NavGraph (Routes.FAVORITES v2.3, Routes.HISTORY v2.2, Routes.RESTAURANT v2.1)
└── di/                      DatabaseModule, RepositoryModule
                             NetworkModule (v2.1), LocationModule (v2.1)
```

---

## 7. Compose 컴포넌트 명세

### MandalaGrid9x9

```kotlin
@Composable
fun MandalaGrid9x9(
    categories: List<FoodCategory>,
    spinState: SpinState,           // Idle / Spinning / Selected(catIndex)
    highlightedIndex: Int,          // 현재 하이라이트 카테고리 인덱스 (-1 = 없음)
    onSpinClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

- `LazyVerticalGrid` 또는 `Column { Row { ... } }` 9×9 구성
- 각 셀: `AnimatedContent` or `animateColorAsState`로 하이라이트 전환
- 블록 경계: `Modifier.border` 또는 `Divider`

### MandalaGrid3x3

```kotlin
@Composable
fun MandalaGrid3x3(
    category: FoodCategory,
    menus: List<MenuItem>,
    spinState: MenuSpinState,
    highlightedIndex: Int,
    onSpinClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### SpinState (화면 1)

```kotlin
sealed class SpinState {
    object Idle : SpinState()
    object Spinning : SpinState()
    data class Selected(val categoryIndex: Int) : SpinState()
}
```

### MenuSpinState (화면 2)

```kotlin
sealed class MenuSpinState {
    object Idle : MenuSpinState()
    object Spinning : MenuSpinState()
    data class Selected(val menuIndex: Int) : MenuSpinState()
}
```

---

## 8. 애니메이션 상세 스펙

### 원형 순환 알고리즘 (공통)

```kotlin
// 시계방향 순환
val CW_ORDER = listOf(0, 1, 2, 3, 4, 5, 6, 7)  // 카테고리 인덱스

suspend fun runSpinAnimation(
    targetIndex: Int,
    onHighlight: (Int) -> Unit,
    onComplete: (Int) -> Unit
) {
    val rounds = 3
    val totalSteps = rounds * 8 + targetIndex + 1
    for (step in 0 until totalSteps) {
        val cwPos = step % 8
        onHighlight(CW_ORDER[cwPos])
        delay(getDelay(step, totalSteps))
    }
    onComplete(CW_ORDER[targetIndex])
}

fun getDelay(step: Int, total: Int): Long {
    val ratio = step.toFloat() / total
    return when {
        ratio < 0.50f -> 70L
        ratio < 0.72f -> 120L
        ratio < 0.88f -> 200L
        ratio < 0.96f -> 320L
        else          -> 480L
    }
}
```

### 선택 완료 효과

- 선택 셀: `animateFloatAsState` → scale 1.0 → 1.1 → 1.0 (pulse)
- 배경색: `animateColorAsState` → 빨강 glow
- 햅틱: `HapticFeedbackConstants.VIRTUAL_KEY`

---

## 9. 비기능 요구사항

| 항목 | 요구사항 |
|------|----------|
| 성능 | 애니메이션 60fps 유지 (Compose recomposition 최소화) |
| 접근성 | TalkBack 지원, `semantics { contentDescription }` 필수 |
| 화면 회전 | `rememberSaveable` + ViewModel로 상태 보존 |
| 최소 해상도 | 360dp × 640dp |
| 오프라인 | 완전 오프라인 동작 |
| 다크모드 | 기본 다크 테마 (라이트 모드 선택 지원) |

---

## 10. UI 디자인 토큰

```kotlin
// Color.kt
val BackgroundDark   = Color(0xFF0F0F14)
val SurfaceDark      = Color(0xFF1A1A24)
val Surface2Dark     = Color(0xFF22222F)
val AccentRed        = Color(0xFFFF4757)
val AccentOrange     = Color(0xFFFFA502)
val AccentGreen      = Color(0xFF2ED573)
val AccentBlue       = Color(0xFF1E90FF)
val TextPrimary      = Color(0xFFF0F0F5)
val TextDim          = Color(0xFFAAAAABC)
```

---

## 11. 개발 마일스톤

| 단계 | 내용 | 기간 |
|------|------|------|
| M1 | 프로젝트 셋업 (Compose + Hilt), 데이터 레이어, 테마 | 1주 |
| M2 | `MandalaGrid9x9` Composable + 원형 스핀 애니메이션 | 1주 |
| M3 | CategoryScreen + CategoryViewModel 완성 | 0.5주 |
| M4 | `MandalaGrid3x3` + MenuScreen + MenuViewModel | 1주 |
| M5 | ResultScreen, Navigation Compose 연결 | 0.5주 |
| M6 | UI 폴리싱, 햅틱, 접근성, QA | 0.5주 |

---

## 12. 향후 확장 고려사항

- ~~메뉴 즐겨찾기 / 제외 기능~~ ✅ v2.3 완료 (FavoriteScreen + MenuSelectScreen 우선 정렬 + 카테고리별 그룹화)
- ~~위치 기반 주변 식당 연동~~ ✅ v2.1 완료 (RestaurantScreen + Kakao Local API)
- 메뉴 커스터마이징 (사용자 직접 추가/삭제)
- ~~오늘 뭐 먹었는지 히스토리 기록~~ ✅ v2.2 완료 (HistoryScreen + 날짜별 그룹화 + 전체 삭제)
- 홈 화면 위젯 지원
- 다국어 지원 (i18n)

---

## 13. v2.1 RestaurantScreen 기술 명세 (위치 기반 음식점 추천)

### 13.1 도메인 모델

```kotlin
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class Restaurant(
    val placeName: String,
    val categoryName: String,        // "음식점 > 한식"
    val addressName: String,
    val roadAddressName: String,
    val phone: String,
    val distanceMeters: Int,
    val placeUrl: String,
    val latitude: Double,
    val longitude: Double
)
```

### 13.2 Repository 인터페이스

```kotlin
interface LocationRepository {
    suspend fun getCurrentLocation(): Coordinates
}

interface RestaurantRepository {
    suspend fun searchNearby(
        query: String,
        coords: Coordinates,
        radiusMeters: Int = 1000,
        size: Int = 15
    ): List<Restaurant>
}
```

### 13.3 UseCase

```kotlin
class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<Coordinates>
}

class SearchNearbyRestaurantsUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    // 비즈니스 규칙: query = "$menuName 음식점"
    suspend operator fun invoke(menuName: String, coords: Coordinates): Result<List<Restaurant>>
}
```

### 13.4 UiState (sealed class)

```kotlin
sealed class RestaurantUiState {
    object Loading : RestaurantUiState()
    object PermissionDenied : RestaurantUiState()
    data class Success(val menuName: String, val restaurants: List<Restaurant>) : RestaurantUiState()
    data class Error(val message: String) : RestaurantUiState()
}
```

### 13.5 NavGraph 확장

- `Routes.RESTAURANT = "restaurant"`
- `Routes.ARG_RESTAURANT_MENU = "menuName"` (ARG_MENU_NAME과 충돌 방지)
- `Routes.restaurant(menuName) = "$RESTAURANT/${Uri.encode(menuName)}"`
- `ResultUiEvent.NavigateToRestaurant(menuName)` 이벤트 추가
- `ResultScreen`에 `onNavigateToRestaurant: (String) -> Unit` 콜백 파라미터 추가

### 13.6 패키지 구조 추가분

```
com.example.mandalunch/
├── data/
│   ├── remote/
│   │   ├── KakaoLocalApiService.kt       (Retrofit interface)
│   │   ├── FusedLocationDataSource.kt    (suspendCancellableCoroutine 래퍼)
│   │   └── dto/
│   │       └── KakaoPlaceDto.kt          (DTO 3종 + Mapper 통합)
│   └── repository/
│       ├── LocationRepositoryImpl.kt
│       └── RestaurantRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── Coordinates.kt
│   │   └── Restaurant.kt
│   ├── repository/
│   │   ├── LocationRepository.kt
│   │   └── RestaurantRepository.kt
│   └── usecase/
│       ├── GetCurrentLocationUseCase.kt
│       └── SearchNearbyRestaurantsUseCase.kt
├── presentation/
│   ├── viewmodel/
│   │   └── RestaurantViewModel.kt
│   └── ui/screen/
│       └── RestaurantScreen.kt
└── di/
    ├── NetworkModule.kt                  (OkHttp/Retrofit/KakaoLocalApiService)
    └── LocationModule.kt                 (FusedLocationProviderClient)
```

### 13.7 Manifest 권한 (v2.1 추가)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-feature
    android:name="android.hardware.location"
    android:required="false" />
```

### 13.8 build.gradle.kts (v2.1 추가)

```kotlin
buildFeatures {
    compose = true
    buildConfig = true   // AGP 8+ 필수
}

defaultConfig {
    buildConfigField("String", "KAKAO_REST_API_KEY", "\"$kakaoRestApiKey\"")
    // local.properties의 kakao.rest.api.key 키 값을 주입
}
```

### 13.9 의사결정 요약

| 결정 사항 | 선택 | 이유 |
|----------|------|------|
| 위치 표현형 | Domain은 `Coordinates`, `android.location.Location`은 Data 격리 | Domain 순수성 |
| 권한 처리 | Screen이 launcher 소유, ViewModel은 `onPermissionResult(Boolean)` 수신 | ViewModel에 Context 누출 차단 |
| API 키 | `local.properties` → `BuildConfig.KAKAO_REST_API_KEY` | VCS 노출 차단 |
| UseCase 반환 | `Result<T>` 래핑 | ViewModel의 명시적 실패 처리 |
| 검색 쿼리 조립 | `SearchNearbyRestaurantsUseCase` 내부 | 비즈니스 규칙을 Domain에 격리 |
| 외부 링크 처리 | Screen에서 `Intent.ACTION_VIEW` 직접 실행 | ViewModel에 Context 누출 차단 |

---

## 14. v2.2 HistoryScreen 기술 명세 (추천 히스토리)

### 14.1 도메인 모델 (기존, 변경 없음)

```kotlin
data class RecommendHistory(
    val id: Int,
    val menuId: Int,   // 현재 0으로 저장 (NavGraph가 menuId 미전달, 표시에 불필요)
    val menuName: String,
    val categoryName: String,
    val recommendedAt: Long
)
```

> **menuId = 0 보관 정책:** NavGraph는 categoryId와 menuName만 라우트 인자로 전달하며 menuId는 전달하지 않는다. HistoryScreen은 menuName과 categoryName만 표시하므로 menuId가 불필요하다. 추후 "히스토리 항목 → 같은 메뉴 다시 추천" 기능 추가 시 이름 기반 lookup으로 해결 가능.

### 14.2 Repository 확장

```kotlin
interface RecommendHistoryRepository {
    fun getHistories(): Flow<List<RecommendHistory>>   // 기존
    suspend fun saveHistory(history: RecommendHistory)  // 기존
    suspend fun deleteAllHistories()                    // v2.2 신규
}
```

### 14.3 UseCase

```kotlin
class GetHistoryUseCase @Inject constructor(
    private val repository: RecommendHistoryRepository
) {
    operator fun invoke(): Flow<List<RecommendHistory>> = repository.getHistories()
}

class DeleteAllHistoryUseCase @Inject constructor(
    private val repository: RecommendHistoryRepository
) {
    suspend operator fun invoke() = repository.deleteAllHistories()
}
```

### 14.4 Presentation 모델 (ViewModel 레이어)

```kotlin
enum class DateGroup(val label: String) {
    TODAY("오늘"), YESTERDAY("어제"), THIS_WEEK("이번 주"), EARLIER("이전")
}

data class HistoryItemUi(
    val id: Int,
    val menuName: String,
    val categoryName: String,
    val recommendedAt: Long,
    val timeLabel: String   // "HH:mm" 포맷 문자열
)

data class HistorySection(
    val group: DateGroup,
    val items: List<HistoryItemUi>
)

data class HistoryUiState(
    val sections: List<HistorySection> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val showDeleteConfirm: Boolean = false
)

sealed class HistoryUiEvent {
    object NavigateBack : HistoryUiEvent()
    object DeleteAllCompleted : HistoryUiEvent()
}
```

### 14.5 NavGraph 확장

- `Routes.HISTORY = "history"` (인자 없는 단순 라우트)
- `MandalartScreen` composable에 `onNavigateToHistory = { navController.navigate(Routes.HISTORY) }` 전달
- `composable(Routes.HISTORY) { HistoryScreen(onBack = { navController.popBackStack() }) }` 등록

### 14.6 MandalartScreen 시그니처 변경

```kotlin
@Composable
fun MandalartScreen(
    onNavigateToMenuSelect: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,   // v2.2 신규
    viewModel: MandalartViewModel = hiltViewModel()
)
```

### 14.7 중복 저장 방지 전략 (ResultViewModel)

`SavedStateHandle["history_saved"]` 플래그 기반:
- **화면 회전:** ViewModel 유지 → init 미호출 → 자연 방지
- **프로세스 복원:** SavedStateHandle 복원 → `alreadySaved = true` → 중복 차단
- **다음 추천:** 새 nav backstack entry = 새 SavedStateHandle → 독립적으로 1회 저장

```kotlin
// ResultViewModel.init 요약
val alreadySaved = savedStateHandle.get<Boolean>("history_saved") ?: false
if (!alreadySaved && menuName.isNotBlank()) {
    saveHistoryUseCase(RecommendHistory(menuId = 0, menuName = menuName,
        categoryName = cat?.name ?: "", recommendedAt = System.currentTimeMillis()))
    savedStateHandle["history_saved"] = true
}
```

### 14.8 날짜 그룹화 로직 — 설계 결정

**DateGroup은 Presentation 레이어에 둔다 (UseCase 아님)**

이유:
1. "오늘/어제/이번 주/이전"은 타임존·로케일·주 시작일 등 표시 관심사 — 도메인 비즈니스 규칙이 아님
2. UseCase는 순수 Domain 레이어이므로 `java.time` 의존을 피하는 것이 바람직
3. 다른 화면에서 같은 데이터를 다른 방식으로 그룹화할 때 Domain은 raw 리스트만 노출

`groupByDate(histories, now: Long, zone: ZoneId)` 함수는 `now` 파라미터를 받아 fake clock 단위 테스트가 가능하도록 설계됨.

### 14.9 의사결정 요약

| 결정 사항 | 선택 | 이유 |
|----------|------|------|
| DateGroup 위치 | Presentation(ViewModel) 레이어 | 표시 관심사 — Domain 순수성 유지 |
| menuId 저장값 | `menuId = 0` | NavGraph 미전달, 히스토리 화면 표시에 불필요 |
| 중복 저장 방지 | `SavedStateHandle` 플래그 | 회전/프로세스 복원/다음 추천 3-케이스 모두 커버 |
| 날짜 그룹화 테스트 | `now: Long` 파라미터화 | fake clock으로 경계 케이스 단위 테스트 가능 |
| DI 모듈 변경 | 변경 없음 | `@Inject constructor` 단독 주입 가능, 기존 바인딩 재사용 |
| DB version | 변경 없음 (version 1 유지) | 스키마 변경 없음 — DAO 메서드만 추가 |

---

## 15. v2.3 FavoriteScreen 기술 명세 (즐겨찾기)

### 15.1 도메인 모델 (기존 활용, 변경 없음)

```kotlin
data class Menu(
    val id: Int,
    val name: String,
    val categoryId: Int,
    val isFavorite: Boolean = false,   // 기존 필드 활용
    val lastRecommendedAt: Long? = null
)
```

> **Room 마이그레이션 불필요:** `MenuEntity.isFavorite` 컬럼이 v1.0 시점부터 이미 존재하므로 DB version 변경 없이 DAO 메서드 추가만으로 기능 구현 가능.

### 15.2 DAO 확장

```kotlin
@Dao
interface MenuDao {
    // 기존
    @Query("SELECT * FROM menus WHERE categoryId = :categoryId")
    fun getByCategory(categoryId: Int): Flow<List<MenuEntity>>

    @Query("UPDATE menus SET lastRecommendedAt = :timestamp WHERE id = :menuId")
    suspend fun updateLastRecommended(menuId: Int, timestamp: Long)

    // v2.3 신규
    @Query("UPDATE menus SET isFavorite = :isFavorite WHERE id = :menuId")
    suspend fun updateFavorite(menuId: Int, isFavorite: Boolean)

    @Query("SELECT * FROM menus WHERE isFavorite = 1 ORDER BY categoryId ASC, name ASC")
    fun getFavorites(): Flow<List<MenuEntity>>

    @Query("UPDATE menus SET isFavorite = 0 WHERE isFavorite = 1")
    suspend fun clearAllFavorites()
}
```

### 15.3 Repository 확장

```kotlin
interface MenuRepository {
    fun getMenusByCategory(categoryId: Int): Flow<List<Menu>>
    suspend fun updateLastRecommended(menuId: Int, timestamp: Long)

    // v2.3 신규
    fun getFavorites(): Flow<List<Menu>>
    suspend fun setFavorite(menuId: Int, isFavorite: Boolean)
    suspend fun clearAllFavorites()
}
```

### 15.4 UseCase

```kotlin
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    // 명시적 setter — 반전은 호출부 책임
    suspend operator fun invoke(menuId: Int, isFavorite: Boolean) {
        repository.setFavorite(menuId, isFavorite)
    }
}

class GetFavoritesUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    operator fun invoke(): Flow<List<Menu>> = repository.getFavorites()
}

class ClearAllFavoritesUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke() = repository.clearAllFavorites()
}
```

### 15.5 Presentation 모델 (ViewModel 레이어)

```kotlin
data class FavoriteCategorySection(
    val categoryName: String,
    val categoryEmoji: String,
    val menus: List<Menu>,
    val categoryPosition: Int   // 그룹 정렬용 (Category.position 사본)
)

data class FavoriteUiState(
    val sections: List<FavoriteCategorySection> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val showClearConfirm: Boolean = false
)

sealed class FavoriteUiEvent {
    object NavigateBack : FavoriteUiEvent()
    object ClearAllCompleted : FavoriteUiEvent()
}
```

### 15.6 MenuSelectViewModel 확장

```kotlin
private fun observeMenus() {
    viewModelScope.launch {
        getMenusByCategoryUseCase(categoryId).collect { list ->
            val sorted = list.sortedWith(
                compareByDescending<Menu> { it.isFavorite }.thenBy { it.id }
            )
            _uiState.update { it.copy(menus = sorted) }
        }
    }
}

fun onToggleFavorite(menu: Menu) {
    // ViewModel 가드: 스핀 중에는 토글 무시
    if (_uiState.value.spinState !is MenuSpinState.Idle) return
    viewModelScope.launch {
        toggleFavoriteUseCase(menu.id, !menu.isFavorite)
    }
}
```

### 15.7 MandalartScreen 시그니처 변경

```kotlin
@Composable
fun MandalartScreen(
    onNavigateToMenuSelect: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,   // v2.3 신규
    viewModel: MandalartViewModel = hiltViewModel()
)
```

### 15.8 NavGraph 확장

- `Routes.FAVORITES = "favorites"` (인자 없는 단순 라우트)
- `composable(Routes.FAVORITES) { FavoriteScreen(onBack = { navController.popBackStack() }) }` 등록
- MandalartScreen 호출부에 `onNavigateToFavorites = { navController.navigate(Routes.FAVORITES) }` 연결

### 15.9 스핀 중 토글 차단 — 이중 가드 전략

스핀 애니메이션 중 즐겨찾기 토글 시 메뉴 리스트 순서가 바뀌면 `highlightedIndex`가 가리키는 셀과 실제 화면 셀이 어긋남. 이를 차단하기 위해 두 위치에서 가드:

| 가드 위치 | 구현 | 효과 |
|----------|------|------|
| UI 가드 | `MenuSelectScreen.MenuCell`에서 `if (menu != null && spinState is MenuSpinState.Idle)`일 때만 하트 아이콘 렌더 | Spinning/Selected 중 아이콘 미노출 → 클릭 불가 |
| ViewModel 가드 | `onToggleFavorite()` 진입 시 `if (spinState !is Idle) return` | 외부 호출에도 안전 (방어적 프로그래밍) |

### 15.10 카테고리 그룹화 알고리즘

```kotlin
private fun groupByCategory(
    favorites: List<Menu>,
    categories: List<Category>
): List<FavoriteCategorySection> {
    if (favorites.isEmpty()) return emptyList()
    val byId = categories.associateBy { it.id }
    return favorites
        .groupBy { it.categoryId }
        .mapNotNull { (catId, menus) ->
            val cat = byId[catId] ?: return@mapNotNull null  // orphan 제외
            FavoriteCategorySection(
                categoryName = cat.name,
                categoryEmoji = cat.emoji,
                menus = menus.sortedBy { it.name },
                categoryPosition = cat.position
            )
        }
        .sortedBy { it.categoryPosition }
}
```

> 설계 의사코드는 정렬 시 `categories.firstOrNull { it.name == section.categoryName }?.position`로 재탐색했으나, 구현은 `categoryPosition` 필드를 섹션에 미리 포함해 O(N log N)으로 단축. 동일한 결과.

### 15.11 의사결정 요약

| 결정 사항 | 선택 | 이유 |
|----------|------|------|
| Room 마이그레이션 | 불필요 (version 1 유지) | `MenuEntity.isFavorite` 이미 존재 |
| ToggleFavoriteUseCase 시그니처 | 명시적 setter `invoke(menuId, isFavorite)` | 내부 toggle 시 추가 DB 조회 발생 — 단순 setter가 깔끔 |
| ClearAllFavoritesUseCase 분리 | UseCase 분리 | HistoryScreen의 `DeleteAllHistoryUseCase`와 일관성 |
| 메뉴 정렬 키 | `compareByDescending(isFavorite).thenBy(id)` | id는 PK라 안정적/결정적 — 토글 시 위치 흔들림 방지 |
| 스핀 중 토글 차단 | UI 가드 + ViewModel 가드 (이중) | 인덱스 매핑 깨짐 방지 — 방어적 프로그래밍 |
| UiEvent 정의 위치 | ViewModel 파일 내 sealed class | 프로젝트 컨벤션 (별도 `event/` 디렉토리 미사용) |
| FavoriteCategorySection 정렬 필드 | `categoryPosition` 사본 포함 | O(N²) 재탐색 회피 |
| DI 모듈 변경 | 변경 없음 | UseCase는 `@Inject constructor` 자동 주입 |
| 헤더 배치 | `[타이틀] → [♥] → [🕐]` | 두 아이콘 나란히 배치로 시각적 균형 |
| FavoriteScreen 내 즉시 해제 | 허용 | UX 일관성 — 동일 ToggleFavoriteUseCase 재사용 |

---

## 16. v2.3 단위 테스트 명세 (2026-06-05 patch)

### 16.1 범위 및 결과

- **테스트 스택:** JUnit4 + MockK + kotlinx-coroutines-test + Turbine (JVM 단위 테스트)
- **테스트 수:** 77건 (`./gradlew test` 클린 실행 시 77/77 PASS, failures=0, errors=0)
- **분포:**
  - UseCase 13건 (GetCategories 3 / SaveHistory 2 / ToggleFavorite 2 / GetFavorites 3 / SearchNearbyRestaurants 3)
  - 유틸/순수 함수 23건 (RestaurantDisplayFormatter 14 / HistoryGroupByDate 9)
  - ViewModel 41건 (Mandalart 8 / MenuSelect 9 / Result 8 / History 6 / Favorite 10)
- **비범위 (향후 확장):** Room DAO 통합 테스트, Compose UI 테스트, Hilt 모듈 테스트, RestaurantRepositoryImpl 네트워크 테스트

### 16.2 테스트 가능성 확보를 위한 프로덕션 코드 리팩토링 (동작 변경 없음)

| 리팩토링 | 위치 | 변경 |
|----------|------|------|
| `cleanCategory` / `formatDistanceWithTime` 유틸 추출 | `presentation/util/RestaurantDisplayFormatter.kt` (신규) | Compose UI 파일에 있던 `private` top-level 함수를 별도 파일로 이동, 가시성 `internal`로 변경 |
| `HistoryViewModel.groupByDate` 가시성 변경 | `presentation/viewmodel/HistoryViewModel.kt` | `private` → `@VisibleForTesting internal` (`androidx.annotation.VisibleForTesting`) |
| 스핀 결정성 주입 | `MandalartViewModel`, `MenuSelectViewModel` | `internal var randomProvider: () -> Int = { (0..7).random() }` 프로퍼티 추가, `onSpinClick()` 내부의 `(0..7).random()`을 이 프로퍼티 호출로 치환 |

> Hilt 생성자에 손대지 않은 이유: Hilt가 모든 생성자 파라미터를 주입 대상으로 보므로 `() -> Int` default 파라미터가 충돌 위험을 만듦. `var` 프로퍼티 노출이 가장 안전.

### 16.3 테스트 의존성

`gradle/libs.versions.toml`:
```toml
junit = "4.13.2"
mockk = "1.13.13"
turbine = "1.2.0"
androidxAnnotation = "1.9.1"
```

`app/build.gradle.kts`:
```kotlin
implementation(libs.androidx.annotation)            // @VisibleForTesting
testImplementation(libs.junit)
testImplementation(libs.mockk)
testImplementation(libs.turbine)
testImplementation(libs.kotlinx.coroutines.test)
```

### 16.4 테스트 인프라

- `app/src/test/java/.../testutil/MainDispatcherRule.kt`
  - `StandardTestDispatcher`를 `Dispatchers.setMain`/`resetMain` 페어로 캡슐화한 JUnit Rule
  - 5개 ViewModel 테스트에서 공용 재사용
  - `StandardTestDispatcher` 선택 이유: 스핀 애니메이션의 `delay()` 가상 시간 진행 필요

### 16.5 핵심 패턴

| 항목 | 패턴 |
|------|------|
| Repository/UseCase mock | `mockk<T>(relaxed = true)` + `every`/`coEvery` 스터빙 |
| Flow 반환 stubbing | `every { repo.getCategories() } returns flowOf(...)` (Flow 자체는 suspend 아님) |
| suspend stubbing | `coEvery { repo.searchNearby(...) } returns ...` / `throws IOException(...)` |
| Flow 종료 검증 | UseCase: `flowOf()` 후 `awaitComplete()` / SharedFlow: `cancelAndIgnoreRemainingEvents()` |
| 이벤트 미발행 검증 | Turbine `expectNoEvents()` |
| `Uri.decode` JVM stub 우회 | `mockkStatic(Uri::class)` + `@After unmockkStatic(Uri::class)` 페어링 |
| 결정성 주입 | `viewModel.randomProvider = { 3 }` 교체 후 `onSpinClick()` 호출 |
| 가상 시간 진행 | `advanceUntilIdle()`로 스핀 `delay` 일괄 통과 |
| 로케일 의존성 회피 | `@Before fun setLocale() { Locale.setDefault(Locale.KOREA) }` (RestaurantDisplayFormatterTest) |

### 16.6 의사결정 요약

| 결정 사항 | 선택 | 이유 |
|----------|------|------|
| `private` 함수 처리 (groupByDate) | `@VisibleForTesting internal` | reflection 회피, IDE 지원 양호 |
| `private` 함수 처리 (RestaurantScreen 유틸) | 별도 유틸 파일 + `internal` | Compose UI 파일은 JVM 테스트에서 import 불가 |
| 스핀 결정성 주입 위치 | `internal var randomProvider` 프로퍼티 | Hilt 생성자 무변경 |
| Test Dispatcher | `StandardTestDispatcher` | `delay()` 가상 시간 진행 필요 |
| ViewModel 인스턴스화 시점 | 각 `@Test` 함수 내부 | init 블록 동작이 테스트별로 달라야 함 (특히 ResultViewModel) |
| 이벤트(SharedFlow) 검증 | Turbine `events.test { }` | replay=0 → 생성 직후 collect 시작 필수 |

### 16.7 발견 사항 (참고)

- `SearchNearbyRestaurantsUseCase.kt:13` KDoc 주석은 "반경 1000m"이나 실제 호출은 `radiusMeters = 3000`. 테스트는 3000을 정확히 검증하므로 회귀 안전성은 확보됨. 차기 정리 시 주석 수정 권고.
- `groupByDate`의 월요일 경계 처리는 코드 동작(`!date.isBefore(mondayThisWeek)`)이 UX상 자연스러우며 테스트로 잠겨 있음. PRD에서 주 시작일 정책을 명문화할 여지는 남아 있으나 현재 동작이 정답.
