# CLAUDE.md — MandaLunch

## 프로젝트 개요

9×9 만다라트 그리드로 점심 메뉴를 추천하는 Android 앱 **MandaLunch**.
Kotlin/Jetpack Compose + Clean Architecture + MVVM + Hilt + Room 구조.
카테고리: 한식 | 중식 | 일식 | 양식 | 아시안 | 분식 | 프랜차이즈 | 건강식

## 하네스: MandaLunch 개발

**목표:** MandaLunch 앱의 UI 설계(Pencil)와 코드 구현(Kotlin/Compose)을 Clean Architecture 원칙에 맞게 수행한다.

**트리거:** UI 설계, 화면 디자인, 기능 구현, 코드 수정 등 모든 개발 작업 요청 시 `lunch-app-dev` 스킬을 사용하라. 단순 질문은 직접 응답 가능.

**변경 이력:**
| 날짜 | 변경 내용 | 대상 | 사유 |
|------|----------|------|------|
| 2026-06-03 | 초기 구성 | 전체 | 신규 프로젝트 하네스 구축 |
| 2026-06-03 | ui-designer 에이전트 추가 | agents/ui-designer.md | 만다라트 UI Pencil 설계 요청 |
| 2026-06-03 | pencil-ui-design 스킬 추가 | skills/pencil-ui-design | UI 설계 가이드 (디자인 토큰, 레이아웃 명세) |
| 2026-06-03 | 앱명 MandaLunch로 변경, 카테고리 확정 | CLAUDE.md, 오케스트레이터 | 사용자 요구사항 반영 |
| 2026-06-04 | PRD 작성 | /Obsidian Vault/MandaLunch/PRD.md | 개발 기준 문서 확정 |
| 2026-06-04 | dev-journal 에이전트 추가 | agents/dev-journal.md | 단계별 개발 과정 Obsidian 기록 |
| 2026-06-04 | PRD v2 반영 하네스 확장 | skills/compose-feature-impl, android-arch-design, lunch-app-dev | 3-screen flow, SpinState, 원형 순환 스핀 패턴 추가 |
| 2026-06-04 | api-integrator 에이전트 추가 | agents/api-integrator.md, lunch-app-dev | 위치 기반 음식점 추천 기능을 위한 외부 API 연동 전담 에이전트 |
| 2026-06-04 | dev-journal PRD 동기화 + 자동 문서화 추가 | agents/dev-journal.md, lunch-app-dev Phase 5 | 구현 완료 후 항상 개발일지 작성 및 Obsidian PRD 동기화 |

---

## 기술 스택

- **언어:** Kotlin
- **UI:** Jetpack Compose + Material3
- **UI 설계:** Pencil (MCP 연동)
- **아키텍처:** Clean Architecture (Presentation → Domain → Data) + MVVM
- **DI:** Hilt
- **DB:** Room
- **상태 관리:** StateFlow + SharedFlow + mutableStateOf

## 빌드 명령어

```bash
./gradlew assembleDebug   # 디버그 빌드
./gradlew test            # 단위 테스트
./gradlew lint            # Lint
```

## 패키지 구조

```
com.example.mandalunch/
├── data/local/room/     (Entity, DAO, Database)
├── data/repository/     (RepositoryImpl)
├── domain/model/        (Menu, Category, RecommendHistory)
├── domain/repository/   (Repository Interface)
├── domain/usecase/      (UseCase)
├── presentation/viewmodel/
├── presentation/ui/screen/   (MandalartScreen, MenuSelectScreen, ResultScreen)
├── presentation/ui/component/
├── presentation/ui/event/
└── di/                  (Hilt Module)
```
