# 📱 개미승리 (antwinner_kotlin) 프로젝트 구조 문서

## 🗂️ 1. 전체 프로젝트 구조

```
antwinner_kotlin/
├── app/                          # 메인 Android 앱
│   ├── src/main/
│   │   ├── java/com/example/antwinner_kotlin/
│   │   │   ├── ui/               # UI 관련 클래스들
│   │   │   │   ├── home/         # 홈 화면 (HomeFragment)
│   │   │   │   ├── stockdetail/  # 종목 상세 화면
│   │   │   │   ├── themedetail/  # 테마 상세 화면
│   │   │   │   ├── themeschedule/# 테마 일정 화면
│   │   │   │   ├── search/       # 검색 화면
│   │   │   │   └── theme/        # 전체 테마 화면
│   │   │   ├── repository/       # 데이터 레이어
│   │   │   ├── network/          # 네트워크 설정
│   │   │   └── model/            # 데이터 모델
│   │   └── res/                  # 리소스 (레이아웃, 문자열, 색상 등)
│   └── build.gradle.kts          # 앱 레벨 빌드 설정
├── flutter_module/               # Flutter 통합 모듈
│   └── lib/
│       └── stock_detail/         # Flutter 종목 상세 페이지
└── gradle/                       # Gradle 설정
    └── libs.versions.toml        # 의존성 버전 관리
```

## 📱 2. 메뉴 구조 및 파일 매칭

### Bottom Navigation 메뉴 (`bottom_nav_menu.xml`)

```xml
1. 대시보드 (navigation_dashboard)        → HomeFragment
2. 테마상세 (navigation_theme_detail)      → ThemeDetailFragment
3. 테마일정 (navigation_theme_schedule)    → ThemeScheduleFragment
```

### Navigation Graph (`nav_graph.xml`)

```xml
startDestination="@id/navigation_dashboard"  # 기본 화면은 대시보드
```

### 각 메뉴별 파일 매칭

#### 1️⃣ 대시보드 (홈화면)

- **Fragment**: `HomeFragment.kt` (약 1,600줄의 대형 파일)
- **Layout**: `fragment_home.xml`
- **주요 기능**:
    - 마켓 티커 (전광판)
    - 검색바 → `SearchActivity`로 이동
    - 테마 히트맵 (`TreemapLayout` 또는 `TreemapWebView`)
    - 핫한 테마 (`HotThemeAdapter`)
    - 주식 탭 (4개 카테고리: 등락률TOP, 거래량TOP, 거래대금TOP, 외국인비율TOP)
    - 상승종목 (`TopRisingStocksPagerAdapter`)
    - 투자 트렌드 (`TrendDayAdapter`)
    - 추천 테마 (`PromisingThemeAdapter`)

#### 2️⃣ 테마상세

- **Fragment**: `ThemeDetailFragment.kt`
- **Activity**: `ThemeDetailActivity.kt` (약 950줄)
- **Layout**: `fragment_theme_detail.xml`, `activity_theme_detail.xml`
- **진입 방법**:
    ```kotlin
    ThemeDetailActivity.newIntent(context, themeId, themeName)
    ```

#### 3️⃣ 테마일정

- **Fragment**: `ThemeScheduleFragment.kt`
- **ViewPager2 구조**: 2개 탭
    - 이슈일정 탭 (`IssueScheduleFragment`)
    - 청약일정 탭 (`SubscriptionScheduleFragment`)
- **관련 Activity**: `IpoDetailActivity.kt` (청약 상세 정보)

### 독립 Activity들

#### 검색 화면

- **Activity**: `SearchActivity.kt`
- **Fragment**: `SearchFragment.kt` (간단한 구조)
- **진입**: 홈화면 검색바 클릭

#### 종목 상세

- **Activity**: `StockDetailActivity.kt` (약 1,172줄의 대형 파일)
- **Flutter 통합**: `StockDetailPage` (Flutter)
- **진입**:
    ```kotlin
    StockDetailActivity.newIntent(context, stockName, stockCode)
    ```

#### 전체 테마

- **Activity**: `AllThemesActivity.kt`
- **진입**: 홈화면 "전체테마 보러가기" 버튼

## 🏗️ 3. 주요 화면별 구조 분석

### 🏠 HomeFragment (핵심 화면)

#### UI 구성 요소 (상하 스크롤 순서)

```xml
1. AppBar (로고 + 검색 아이콘)
2. 마켓 티커 (전광판 스크롤)
3. "테마를 한눈에" + TreemapLayout (또는 TreemapWebView)
4. "오늘 핫한 테마" + 가로 RecyclerView
5. "전체테마 보러가기" 버튼
6. "최근 테마 트렌드" + RecyclerView
7. "다가오는 테마 일정" + 가로 RecyclerView (PromisingThemeAdapter)
8. "실시간 시장 현황" + ViewPager2 (4개 탭)
9. "가장 많이 오른 종목은?" + 기간 필터 + ViewPager2
```

#### 주요 어댑터들

```kotlin
- TreemapAdapter          # 테마 히트맵 (또는 TreemapWebView 사용)
- HotThemeAdapter         # 핫한 테마 가로 스크롤
- TrendDayAdapter         # 투자 트렌드 세로 리스트
- PromisingThemeAdapter   # 추천 테마 가로 스크롤
- TopRisingStocksPagerAdapter  # 상승종목 페이저 (기간별)
```

#### 데이터 로드 과정

```kotlin
loadData() →
  fetchMarketIndices()      # 마켓 티커
  fetchThemeFluctuations()  # 히트맵 데이터
  fetchTrendData()          # 투자 트렌드
  fetchHotThemes()          # 핫한 테마
  fetchPromisingThemes()    # 추천 테마
  fetchTopRisingStocks()    # 상승종목 (기간별: 1W, 1M, 3M, 6M)
```

### 📊 StockDetailActivity (종목 상세)

#### UI 구성 (탭 구조)

```kotlin
MainTabPagerAdapter → 3개 탭:

1. 차트 탭 (ChartFragment)
   - 종목 차트 데이터 표시

2. 왜 올랐을까? 탭 (WhyRiseFragment)
   - 급등 이력 및 이유 표시

3. 종목정보 탭 (StockInfoFragment)
   - 기본 정보
   - 투자 지표 분석 (InvestmentIndicatorAnalyzer 사용)
   - 실적 차트 (연간/분기별)
   - 거래 동향
   - 뉴스
   - 공시
   - 재무 정보
```

#### 상단 정보 섹션

```kotlin
- 종목명, 현재가, 등락률
- 테마 칩 (이미지 포함)
- 투자자 수, 평균 수익률
- 최근 상승 정보 (API 연동)
- 상승 히스토리 버튼 → "왜 올랐을까?" 탭으로 이동
```

#### 주요 Fragment 구조

```kotlin
StockInfoFragment 내부:
- InvestmentIndicatorAnalyzer: PER, PBR, ROE, ROA, 부채비율 등 분석
- TradingTrendFullAdapter: 거래 동향 데이터
- 실적 차트 (BarChart): 연간/분기별 전환 가능
- 뉴스, 공시, 재무 정보 탭들
```

**참고**: `StockDetailPagerAdapter`는 5개 Fragment를 가지지만, 이는 다른 컨텍스트에서 사용되거나 향후 확장을 위한 것으로 보입니다. 현재 메인 탭은 `MainTabPagerAdapter`의 3개 탭을 사용합니다.

### 🎯 ThemeDetailActivity (테마 상세)

#### UI 구성 요소

```kotlin
1. 헤더 (테마명, 아이콘, 등락률, 순위)
2. 테마 이슈 추세 차트 (기간 필터: 1m, 3m, 6m, 1y)
3. 테마주 리스트 (접기/펼치기, 최대 5개 → 전체)
4. 급등 이유 리스트 (페이징, 초기 3개 → 더보기)
5. 관련 뉴스 리스트 (접기/펼치기)
```

#### 순위 표시 로직

```kotlin
- 주간 상위 3위: "🔥 주간 상승 N위 테마"
- 월간 1~10위: "🔥 월간 상승 N위 테마"
- 월간 하위 10등: "💧 월간 상승 N위 테마"
- 기타: "🎯 월간 상승 N위 테마"
```

### 📅 ThemeScheduleFragment (테마 일정)

#### UI 구성

```kotlin
ViewPager2 + TabLayout 구조:

1. 이슈일정 탭 (IssueScheduleFragment)
   - 오늘의 이슈 종목 리스트

2. 청약일정 탭 (SubscriptionScheduleFragment)
   - IPO 청약 일정 리스트
   - IpoDetailActivity로 이동 가능
```

## 🌐 4. API 및 데이터 플로우

### 주요 API 엔드포인트 매칭

#### HomeFragment

```kotlin
- /api/stock_keywords              → 투자 트렌드
- /api/market_indices (또는 /api/jisu) → 마켓 티커
- /api/average-fluctuation         → 테마 히트맵
- /api/keywords/ai_info            → AI 추천
- /api/stocks/top_fluctuations     → 상승률 TOP
- /api/stocks/top_volume            → 거래량 TOP
- /api/stocks/top_trade_amount     → 거래대금 TOP
- /api/stocks/top_foreigners       → 외국인비율 TOP
- /api/promising_themes            → 추천 테마
- /api/stocks (period 파라미터)     → 상승종목 (기간별)
```

#### StockDetailActivity

```kotlin
- /api/stock_people/{name}         → 종목 기본정보
- /api/stocks/{name}               → 급등 히스토리
- /api/chart/{name}                → 차트 데이터
```

#### StockInfoFragment (종목정보 탭)

```kotlin
- /api/trading_data/{name}        → 거래 동향
- /api/financial_data/{name}      → 재무 정보
- /api/investment_indicators/{name} → 투자 지표
- /api/comprehensive_analysis/{name} → 종합 분석
- /api/news/{name}                 → 뉴스
- /api/disclosure/{name}           → 공시
```

#### ThemeDetailActivity

```kotlin
- /api/all-themas/{name}          → 테마 정보
- /api/stocks/daily-keyword-count/{name} → 차트 데이터
- /api/stocks/bracket-keyword/{name}     → 급등 이유
- /api/thema_issue_detail/{name}   → 테마 이슈 상세
- /api/thema_issue_ranking         → 주간/월간 순위
- /api/news_og/title/{keyword}     → 관련 뉴스
```

#### ThemeScheduleFragment

```kotlin
- /api/stocks                      → 오늘의 이슈 종목
- /api/ipos                        → IPO 청약 일정
- /api/ipo_detailed_info/{name}    → IPO 상세 정보
```

#### SearchActivity

```kotlin
- /api/search_stock/{query}        → 종목 검색
- /api/autocomplete/{query}        → 자동완성
- /api/thema_autocomplete/{query}  → 테마 자동완성
- /api/latest_keywords             → 최근 검색어
```

### Repository 패턴

```kotlin
TrendRepository:
- getThemeFluctuations()    # 테마 등락률
- getTopRisingStocks()      # 상승종목 (기간별)
- searchTheme()             # 테마 검색
- getAIKeywords()           # AI 추천
- getMarketIndices()        # 마켓 티커
- getTrendData()            # 투자 트렌드

StockRepository:
- getTopFluctuations()      # 상승률 TOP
- getTopVolume()            # 거래량 TOP
- getTopTradeAmount()       # 거래대금 TOP
- getTopForeigners()        # 외국인비율 TOP
```

### 베이스 URL

```kotlin
BASE_URL = "https://antwinner.com/"
```

## 🎨 5. UI 컴포넌트 및 커스텀 뷰

### 커스텀 뷰들

```kotlin
TreemapLayout              # 테마 히트맵 시각화 (네이티브)
TreemapWebView             # 테마 히트맵 시각화 (WebView 기반)
SimpleBarChartView         # 차트 컴포넌트
ThemeGridLayoutManager     # 테마 그리드 레이아웃
TopRisingStocksPagerAdapter # 상승종목 페이저
```

### 주요 어댑터 클릭 이벤트

```kotlin
HotThemeAdapter → ThemeDetailActivity
TreemapAdapter → ThemeDetailActivity
TopRisingStockAdapter → StockDetailActivity
TrendDayAdapter → ThemeDetailActivity
PromisingThemeAdapter → ThemeDetailActivity
ThemeDetailStockAdapter → StockDetailActivity (종목 클릭 시)
```

## 🔧 6. 개발 시 주요 포인트

### 프로젝트 설정

- **앱 이름**: "개미승리" (`strings.xml`)
- **패키지**: `com.example.antwinner_kotlin`
- **베이스 URL**: `https://antwinner.com/`

### 네비게이션 플로우

```
MainActivity (bottom nav)
├── HomeFragment (기본)
│   ├── 검색바 클릭 → SearchActivity
│   ├── 테마 클릭 → ThemeDetailActivity
│   ├── 종목 클릭 → StockDetailActivity
│   └── "전체테마 보러가기" → AllThemesActivity
├── ThemeDetailFragment
│   └── 테마/종목 클릭 → ThemeDetailActivity / StockDetailActivity
└── ThemeScheduleFragment
    └── IPO 클릭 → IpoDetailActivity

독립 Activity들:
├── SearchActivity ← 홈화면 검색바
├── StockDetailActivity ← 종목 클릭
├── ThemeDetailActivity ← 테마 클릭
├── AllThemesActivity ← 홈화면 "전체테마 보러가기"
└── IpoDetailActivity ← 청약일정 클릭
```

### Flutter 통합

```kotlin
// 네이티브에서 Flutter 호출 (현재는 사용되지 않을 수 있음)
val intent = Intent(this, FlutterActivity::class.java)
intent.putExtra("route", "/stockDetail?name=$stockName&code=$stockCode")
startActivity(intent)

// 실제로는 StockDetailActivity가 네이티브로 구현되어 있음
```

### 데이터 로딩 전략

```kotlin
1. 네트워크 확인 (NetworkUtil.isNetworkAvailable())
2. API 호출 시도
3. 실패 시 더미 데이터 로드 (loadDummyData())
4. SwipeRefreshLayout으로 수동 새로고침 지원
5. 자동 갱신 타이머 (5분 간격, 선택적)
```

### 주요 의존성

- **Retrofit**: 네트워크 통신
- **Gson**: JSON 파싱
- **Glide**: 이미지 로딩
- **ViewPager2**: 탭/페이저 구현
- **RecyclerView**: 리스트 표시
- **MPAndroidChart**: 차트 시각화
- **Timber**: 로깅
- **Coroutines**: 비동기 처리

## 📝 7. 추가 참고 사항

### 파일 크기 및 복잡도

- **HomeFragment.kt**: 약 1,600줄 (대형 파일, 리팩토링 고려)
- **StockDetailActivity.kt**: 약 1,172줄 (대형 파일)
- **ThemeDetailActivity.kt**: 약 950줄

### 데이터 모델 위치

```kotlin
- ui/home/model/          # 홈 화면 관련 모델
- ui/stockdetail/model/   # 종목 상세 관련 모델
- model/                  # 공통 모델
```

### 네트워크 클라이언트

프로젝트 내에 여러 네트워크 클라이언트가 존재할 수 있습니다:
- `RetrofitClient.kt` (여러 위치)
- `ApiClient.kt`
- `ApiService.kt` (여러 위치)

실제 사용되는 클라이언트를 확인하여 일관성 유지 필요.

---

**마지막 업데이트**: 2024년 (프로젝트 구조 기반)

