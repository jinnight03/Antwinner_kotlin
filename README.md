# 📱 개미승리 (Antwinner)

한국 주식 시장 정보를 제공하는 Android 애플리케이션입니다. 실시간 시장 현황, 테마 분석, 종목 상세 정보 등을 제공합니다.

## ✨ 주요 기능

### 🏠 홈 화면
- **실시간 마켓 티커**: 주요 지수 및 시장 현황 실시간 표시
- **테마 히트맵**: 테마별 등락률을 한눈에 볼 수 있는 시각화
- **핫한 테마**: 오늘 가장 주목받는 테마 추천
- **상승 종목**: 기간별(1주, 1개월, 3개월, 6개월) 상승 종목 분석
- **투자 트렌드**: 최근 투자 트렌드 및 키워드 분석
- **실시간 시장 현황**: 등락률/거래량/거래대금/외국인비율 TOP 종목

### 📊 종목 상세
- **차트 분석**: 종목 가격 차트 및 기술적 분석
- **투자 지표**: PER, PBR, ROE, ROA, 부채비율 등 종합 분석
- **급등 이력**: "왜 올랐을까?" - 종목 급등 이유 및 이력 분석
- **재무 정보**: 연간/분기별 실적 차트 및 재무 데이터
- **거래 동향**: 투자자별 매매 동향 분석
- **뉴스 & 공시**: 관련 뉴스 및 공시 정보

### 🎯 테마 분석
- **테마 상세**: 테마별 등락률, 순위, 관련 종목 정보
- **테마 이슈 추세**: 기간별 테마 이슈 추세 차트
- **급등 이유**: 테마 급등 배경 및 이유 분석
- **관련 뉴스**: 테마 관련 최신 뉴스

### 📅 테마 일정
- **이슈 일정**: 오늘의 이슈 종목 일정
- **청약 일정**: IPO 청약 일정 및 상세 정보

### 🔍 검색
- **종목 검색**: 종목명/종목코드 검색
- **테마 검색**: 테마 키워드 검색 및 자동완성
- **인기 검색어**: 최근 인기 검색어 제공

### 🎨 추가 기능
- **홈 위젯**: 테마 정보를 홈 화면에서 바로 확인
- **자동 업데이트**: Google Play In-App Updates를 통한 자동 업데이트
- **다크 모드 지원**: 시스템 설정에 따른 테마 자동 전환

## 🛠 기술 스택

### 언어 및 프레임워크
- **Kotlin**: 메인 개발 언어
- **Android SDK**: 최소 SDK 24, 타겟 SDK 35
- **Flutter**: 일부 화면 Flutter 모듈 통합

### 주요 라이브러리
- **Retrofit 2.9.0**: REST API 통신
- **Gson**: JSON 파싱
- **Glide 4.16.0**: 이미지 로딩 및 캐싱
- **MPAndroidChart v3.1.0**: 차트 시각화
- **Navigation Component**: 화면 간 네비게이션
- **ViewPager2**: 탭 및 페이저 구현
- **Coroutines**: 비동기 처리
- **Timber**: 로깅

### 아키텍처
- **MVVM 패턴**: ViewModel 기반 데이터 관리
- **Repository 패턴**: 데이터 레이어 분리
- **Data Binding & View Binding**: UI 바인딩

## 📁 프로젝트 구조

```
antwinner_kotlin/
├── app/                          # 메인 Android 앱
│   ├── src/main/
│   │   ├── java/com/example/antwinner_kotlin/
│   │   │   ├── ui/               # UI 관련 클래스
│   │   │   │   ├── home/         # 홈 화면
│   │   │   │   ├── stockdetail/  # 종목 상세
│   │   │   │   ├── themedetail/  # 테마 상세
│   │   │   │   ├── themeschedule/# 테마 일정
│   │   │   │   ├── search/       # 검색
│   │   │   │   └── theme/        # 전체 테마
│   │   │   ├── repository/       # 데이터 레이어
│   │   │   ├── network/          # 네트워크 설정
│   │   │   ├── model/            # 데이터 모델
│   │   │   └── widget/           # 홈 위젯
│   │   └── res/                  # 리소스 파일
│   └── build.gradle.kts          # 앱 빌드 설정
├── flutter_module/               # Flutter 통합 모듈
│   └── lib/stock_detail/          # Flutter 종목 상세 페이지
├── gradle/                       # Gradle 설정
│   └── libs.versions.toml        # 의존성 버전 관리
└── docs/                         # 문서
    ├── PROJECT_STRUCTURE.md      # 프로젝트 구조 상세 문서
    ├── COMPREHENSIVE_ANALYSIS_API_SPEC.md  # API 스펙 문서
    └── RELEASE_NOTES_v1.7.md     # 릴리즈 노트
```

자세한 프로젝트 구조는 [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)를 참고하세요.

## 🚀 시작하기

### 필수 요구사항
- Android Studio Hedgehog (2023.1.1) 이상
- JDK 8 이상
- Android SDK 24 이상
- Gradle 8.0 이상

### 설치 방법

1. **저장소 클론**
   ```bash
   git clone https://github.com/jinnight03/Antwinner_kotlin.git
   cd Antwinner_kotlin
   ```

2. **프로젝트 열기**
   - Android Studio에서 프로젝트 열기
   - Gradle 동기화 대기

3. **환경 변수 설정 (선택사항)**
   - Flutter 엔진 경로 설정 (Flutter 모듈 사용 시)
   - Keystore 파일 설정 (릴리즈 빌드 시)

4. **빌드 및 실행**
   ```bash
   ./gradlew assembleDebug
   ```
   또는 Android Studio에서 Run 버튼 클릭

### 빌드 설정

#### Debug 빌드
```bash
./gradlew assembleDebug
```

#### Release 빌드
환경 변수 설정 필요:
- `KEYSTORE_FILE`: Keystore 파일 경로
- `KEYSTORE_PASSWORD`: Keystore 비밀번호
- `KEY_ALIAS`: 키 별칭
- `KEY_PASSWORD`: 키 비밀번호

```bash
./gradlew assembleRelease
```

## 🌐 API 정보

### Base URL
```
https://antwinner.com/api/
```

### 주요 API 엔드포인트

#### 종목 관련
- `GET /stock_people/{name}` - 종목 기본 정보
- `GET /stocks/{name}` - 종목 상세 정보
- `GET /chart/{name}` - 차트 데이터
- `GET /trading_data/{name}` - 거래 동향
- `GET /financial_data/{name}` - 재무 정보
- `GET /investment_indicators/{name}` - 투자 지표
- `GET /comprehensive_analysis/{name}` - 종합 분석
- `GET /news/{name}` - 뉴스 정보
- `GET /disclosure/{name}` - 공시 정보

#### 테마 관련
- `GET /all-themas/{name}` - 테마 정보
- `GET /stocks/daily-keyword-count/{name}` - 테마 이슈 추세
- `GET /stocks/bracket-keyword/{name}` - 급등 이유
- `GET /thema_issue_ranking` - 테마 순위
- `GET /promising_themes` - 추천 테마

#### 검색 관련
- `GET /search_stock/{query}` - 종목 검색
- `GET /autocomplete/{query}` - 자동완성
- `GET /thema_autocomplete/{query}` - 테마 자동완성

자세한 API 스펙은 [COMPREHENSIVE_ANALYSIS_API_SPEC.md](./COMPREHENSIVE_ANALYSIS_API_SPEC.md)를 참고하세요.

## 📱 화면 구성

### Bottom Navigation
1. **대시보드** (HomeFragment) - 홈 화면
2. **테마상세** (ThemeDetailFragment) - 테마 상세 화면
3. **테마일정** (ThemeScheduleFragment) - 테마 일정 화면

### 주요 Activity
- `MainActivity` - 메인 화면 (Bottom Navigation)
- `StockDetailActivity` - 종목 상세 화면
- `ThemeDetailActivity` - 테마 상세 화면
- `SearchActivity` - 검색 화면
- `AllThemesActivity` - 전체 테마 화면
- `IpoDetailActivity` - IPO 상세 화면

## 📦 버전 정보

- **현재 버전**: 1.8 (versionCode: 21)
- **최소 SDK**: 24 (Android 7.0)
- **타겟 SDK**: 35 (Android 15)

### 주요 업데이트 내역
- **v1.8**: 최신 안정성 개선
- **v1.7**: 자동 업데이트 기능 추가
  - Google Play In-App Updates 통합
  - 백그라운드 다운로드 지원
  - 사용자 친화적인 업데이트 UI

자세한 릴리즈 노트는 [RELEASE_NOTES_v1.7.md](./RELEASE_NOTES_v1.7.md)를 참고하세요.

## 🔧 개발 가이드

### 코드 스타일
- Kotlin 코딩 컨벤션 준수
- 클린 코드 원칙 적용
- MVVM 아키텍처 패턴 사용

### 주요 개발 포인트
- **네트워크 처리**: Retrofit + Coroutines 사용
- **이미지 로딩**: Glide 사용 (캐싱 자동 처리)
- **데이터 바인딩**: Data Binding 및 View Binding 활용
- **비동기 처리**: Coroutines 사용

### 테스트
```bash
# Unit 테스트 실행
./gradlew test

# Instrumented 테스트 실행
./gradlew connectedAndroidTest
```

## 📄 라이선스

이 프로젝트는 개인 프로젝트입니다.

## 👥 기여

이슈 및 개선 제안은 GitHub Issues를 통해 제출해주세요.

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

**개미승리** - 스마트한 주식 투자를 위한 정보 플랫폼 📈

