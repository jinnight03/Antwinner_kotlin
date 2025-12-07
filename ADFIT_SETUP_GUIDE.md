# 카카오 AdFit 네이티브 광고 설정 가이드

## 현재 상태
- 광고 레이아웃 및 UI 구현 완료 ✅
- 광고 영역: "테마 힙트맵"과 "오늘 핫한 테마" 사이에 위치
- 광고 로직: 임시 주석 처리 (AdFit SDK 설정 후 활성화 필요)
- **SDK 설치 완료** ✅
  - 의존성 추가: `com.kakao.adfit:ads-base:3.11.10`
  - Maven 저장소 설정 완료
  - AndroidManifest.xml 권한 및 서비스 등록 완료

## AdFit 광고 활성화 단계

### 1. 카카오 AdFit 광고 단위 생성

1. [카카오 AdFit 관리자 페이지](https://adfit.kakao.com/) 접속
2. 로그인 및 앱 등록
3. 네이티브 광고 단위 생성
   - 광고 유형: **네이티브 (Native)**
   - 광고 크기: **1200x600 메인 이미지 피드**
4. 광고 단위 ID 발급 받기 (예: `DAN-xxxxxxxxx`)

### 2. 의존성 버전 확인 및 조정

현재 `app/build.gradle.kts`에 추가된 의존성:
```kotlin
implementation("com.kakao.adfit:ads-base:3.11.10")
```

**문제 발생 시:**
- 최신 버전 확인: [AdFit Android 가이드](https://adfit.github.io/wiki/android-guide/)
- 다른 artifact ID 시도:
  ```kotlin
  implementation("com.kakao.sdk:adfit:3.11.10")
  // 또는
  implementation("com.kakao.adfit:adfit-native:3.11.10")
  ```

### 3. Gradle Sync 및 SDK 다운로드

```bash
# Android Studio에서 또는
./gradlew clean
./gradlew build
```

### 4. 광고 코드 활성화

**HomeFragment.kt 수정:**

#### 4-1. Import 주석 해제 (85-87번째 줄)
```kotlin
// 주석 해제
import com.kakao.adfit.ads.na.NativeAdLoader
import com.kakao.adfit.ads.na.NativeAd
```

#### 4-2. setupNativeAd 호출 활성화 (181-182번째 줄)
```kotlin
// 주석 해제
setupNativeAd(view)
```

#### 4-3. setupNativeAd 함수 주석 해제 (483-562번째 줄)
```kotlin
// 함수 전체 주석 해제 (/* ... */ 제거)
private fun setupNativeAd(view: View) {
    // ...
}
```

#### 4-4. 광고 단위 ID 설정 (494번째 줄)
```kotlin
val nativeAdLoader = NativeAdLoader.Builder(requireContext())
    .adUnitId("YOUR_AD_UNIT_ID") // ← 발급받은 광고 단위 ID로 교체
    .build()
```

### 5. AndroidManifest.xml 설정 확인

#### 5-1. 권한 확인
이미 추가되어 있음:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```

#### 5-2. AdFitService 등록
AndroidManifest.xml의 `<application>` 태그 내에 AdFitService가 등록되어 있습니다:
```xml
<!-- 카카오 AdFit Service -->
<service
    android:name="com.kakao.adfit.ads.AdFitService"
    android:exported="false" />
```

### 6. 빌드 및 테스트

```bash
./gradlew assembleDebug
```

## 파일 위치

- **광고 레이아웃**: `app/src/main/res/layout/item_native_ad.xml`
- **홈 화면 레이아웃**: `app/src/main/res/layout/fragment_home.xml` (141-146번째 줄)
- **광고 로직**: `app/src/main/java/.../ui/home/HomeFragment.kt` (483-562번째 줄)

## 문제 해결

### AdFit SDK를 찾을 수 없는 경우

1. **Maven 저장소 확인** (`build.gradle`):
   ```groovy
   maven { url 'https://devrepo.kakao.com/nexus/content/groups/public/' }
   ```

2. **의존성 버전 변경 시도**:
   ```kotlin
   implementation("com.kakao.adfit:ads-base:3.19.5") // 최신 버전
   ```

3. **수동 SDK 다운로드**:
   - [AdFit 가이드](https://adfit.github.io/wiki/android-guide/)에서 SDK 수동 다운로드

### 광고가 표시되지 않는 경우

1. **광고 단위 ID 확인**
2. **네트워크 연결 확인**
3. **로그 확인**:
   ```kotlin
   Log.d("HomeFragment", "네이티브 광고 로드 완료")
   Log.w("HomeFragment", "네이티브 광고 로드 실패: ...")
   ```
4. **테스트 광고 활성화**: AdFit 대시보드에서 테스트 모드 설정

## 광고 정책 준수

- 광고와 콘텐츠를 명확하게 구분 (현재 "광고" 라벨 표시 중)
- 광고를 가리거나 클릭을 강요하지 않음
- 광고 없이 앱 기능 사용 가능

## 참고 자료

- [AdFit Android 가이드](https://adfit.github.io/wiki/android-guide/)
- [AdFit 시작하기](https://adfit.kakao.com/web/html/use.html)
- [네이티브 광고 구현](https://adfit.github.io/wiki/android-guide/#native-ads)

